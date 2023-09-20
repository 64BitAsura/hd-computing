(ns main
  (:require
   [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
   [tech.v3.datatype.functional :as dtype-fn]
   [tech.v3.tensor :as dtt]
   [clojure.core.reducers :as r]
   [tech.v3.datatype :as dtype]
   [serializer.serial :as serializer]
   [seed.seed :as seed]
   [seed.trigram :as trigram]
   [gigasquid.vsa-base :as vb]))

(tufte/add-basic-println-handler! {})

(use '[clojure.string :only (trim split)])

;; First thing is that we need a global memory to remember all of our hdv
(vb/reset-hdv-mem!)

(trigram/warmup-trigrams-from-seed (seed/seed))

(println "training")

(seed/load-seed "lang.vsa")

(defn if-not-three-add-space [text] (if (< (count text) 3) (apply str text (into []  (repeatedly (- 3 (count text)) #(str " ")))) text))

(defn trigrams [text] (map (fn ([pos] (subs (if-not-three-add-space text) pos (+ pos 3)))) (range 0 (- (count (if-not-three-add-space text)) 2))))

(defn extract-file [s] (second (next (re-find #"(trainingData/dicts|testing)/(.+)" s))))

(defn zero [] 0)

(def zero_vsa (dtt/->tensor (repeatedly 1e4 zero)))
(def counter (atom 0))

(defn dummy-counter-fn [vsa] (swap! counter inc) (println  "called " @counter " times") vsa)
(defn dummy-fn [line vsa] (println "\r log " line vsa (map str (into [] (concat (chars (char-array vsa))))) (count (map str (into [] (concat (chars (char-array vsa))))))) nil)
(defn text-profiling [line]  (->> (trigrams
                                   (trim (clojure.string/lower-case line)))
                                  (filter some?)
                                  (map
                                   #(or (vb/get-hdv %)
                                        (apply
                                         trigram/trigram-dot-product
                                         (map str (into [] (concat (chars (char-array %))))))
                                        zero_vsa))
                                  (filter some?)
                                  (apply dtype-fn/+)))

(seed/load-seed "dicts.vsa")

(doseq [filePath (serializer/list-files "trainingData/dicts/")]
  (let [lang (str  "dicts/" (extract-file filePath))]
    (when (nil? (vb/get-hdv lang))
      (tufte/profile {} (p :lang
                           (try (let [n-grams (doall (->>
                                                      (serializer/lazy-file-lines filePath)
                                                      (remove clojure.string/blank?)
                                                      (pmap text-profiling)
                                                      (filter some?)))]
                                  (println "n-gramed for " lang)
                                  (serializer/serialize-negative-vector-as-string
                                   (lazy-seq [[lang
                                               (dtt/->tensor
                                                (vb/clip
                                                 (apply dtype-fn/+ n-grams))
                                                :container-type :native-heap
                                                :resource-type nil
                                                :datatype :byte)]]) "dicts.vsa")) (catch Error e (println "exception on " lang " ex eption " e))))))))

(def lang-cleanup-mem (atom {}))

(seed/load-seed "dicts.vsa" lang-cleanup-mem)

(defn byte-me [vsa] (dtt/->tensor vsa :datatype :byte :resource-type :gc))

;; (println (vb/query-cleanup-mem-verbose
;;           lang-cleanup-mem
;;           (->> (split "bantu-christian, orthodox-shia-neohinduism" #"-")
;;                (pmap text-profiling)
;;                (pmap vb/clip)
;;                (reduce vb/bundle-op)
;;                (byte-me))))

;; (println (vb/query-cleanup-mem-verbose lang-cleanup-mem (vb/clip (text-profiling "doctor of audiology"))))

(def pos (atom {}))
(def numeric (map int (into [] (concat  (range (int 0) (inc (int 110)))))))
(run! #(vb/add-to-cleanup-mem % pos) numeric)
(println (count pos))

(doseq [filePath (serializer/list-files "testing/dicts")]
  (when (= filePath "testing/dicts/email-ids")
    (let [lang (str (extract-file filePath))
          results
          (doall (->> (serializer/lazy-file-lines filePath)
                      (remove clojure.string/blank?)
                    ;(pmap text-profiling)
                    ;(filter some?)
                      (pmap #(->> %
                                  (text-profiling)
                                  (filter some?)))
                      (map-indexed #(dtype-fn/* (get @pos %1) %2))
                      (apply dtype-fn/+)
                      (vb/clip)
                      (byte-me)
                      (dtype-fn/* (get @lang-cleanup-mem "dicts/email-ids"))
                      (byte-me)
                      (vb/query-cleanup-mem-verbose pos)
                      (map #(str "\n" %))
                      (reverse)

                    ;; (pmap #(vb/query-cleanup-mem lang-cleanup-mem %))
                    ;; (pmap first)
                    ;; (pmap dummy-counter-fn)
                      ))]
      (println (str "\r \r \r tags for the column " lang ".txt are "  (apply str (take 30 results))) "\n \n"))))
;)

;; (def lang-query-dan (text-profiling "den foerste valgmulighed at tage konventionernes indhold op til fornyet overvejelse for at fylde alle hullerne ud er en kompliceret og risikopraeget mulighed kompliceret fordi krigsfoerelsen til stadighed skifter karakter og risikopraeget fordi det ikke kan udelukkes at aabningen af konventionerne for nye forhandlinger kan foere til at der ikke opnaas nogen ny enighed"))
;; (def lang-query-est (text-profiling "eesistujariik alustab toeoed euroopa liidu jaoks kriitilisel ajal ja ma soovin talle edu samas tunnistan et mul on ka moningaid kartusi "))
;; (def lang-query-eng (text-profiling "for many europeans a hens egg is a welcome part of their breakfast 
;; "))
;; (def lang-query-fra (text-profiling "en attenuant la valeur limite journaliere ou en adoptant des mesures de lutte contre le probleme de la pollution"))
;; (def lang-query-ell (text-profiling "den arkei na thespisei e  austerous kanones gia tis ekpompes dioxeidiou tou anthraka"))
;; (def lang-query-pol (text-profiling "innymi slowy finansowanie badan i rozwoju z ramienia rzadow europejskich jest ograniczone splacalne i w zaden sposob nie wplywa na konkurencyjnosc boeinga"))
;; (def lang-query-nld (text-profiling "en als we de economische samenwerking willen versterken moeten we natuurlijk onze inspanningen opvoeren om ons doel van een vrijhandelszone in  te bereiken"))

;; (vb/reset-hdv-mem!)

;; ;(seed/load-seed "seed.vsa")

;; ;(println (vb/query-cleanup-mem-verbose query))

;; (vb/reset-hdv-mem!)

;; (seed/load-seed "lang.vsa")

;; (println (vb/query-cleanup-mem lang-cleanup-mem (dtt/->tensor
;;                                                  (vb/clip lang-query-dan) :datatype :byte)))
;; (println (vb/query-cleanup-mem lang-cleanup-mem lang-query-est))
;; (println (vb/query-cleanup-mem lang-cleanup-mem lang-query-eng))
;; (println (vb/query-cleanup-mem lang-cleanup-mem lang-query-fra))
;; (println (vb/query-cleanup-mem lang-cleanup-mem lang-query-ell))
;; (println (vb/query-cleanup-mem lang-cleanup-mem lang-query-pol))
;; (println (first (vb/query-cleanup-mem lang-cleanup-mem lang-query-nld)))






