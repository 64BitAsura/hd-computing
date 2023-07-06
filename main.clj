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

(use '[clojure.string :only (trim)])

;; First thing is that we need a global memory to remember all of our hdv
(vb/reset-hdv-mem!)

(trigram/warmup-trigrams-from-seed (seed/seed))

(println "training")

(seed/load-seed "lang.vsa")

(defn trigrams [text] (map (fn ([pos] (subs text pos (+ pos 3)))) (range 0 (- (count text) 2))))

(defn extract-file [s] (second (next (re-find #"(trainingData|testing)/(.+?)\.txt" s))))
(defn zero [] 0)
(defn safe-println [& more]
  (.write *out* (str (clojure.string/join " " more) "\r"))
  (.flush *out*))
(defn text-profiling [line] (reduce
                             #(dtt/->tensor
                               (try
                                 (vb/bundle-op
                                  (or (vb/get-hdv %1) %1)
                                  (vb/get-hdv %2))
                                 (catch Exception e (println line) %1))
                               :container-type
                               :jvm-heap
                               :resource-type
                               :gc
                               :datatype
                               :int)
                             (trigrams (trim line))))
;; (defn imperative-reduce
;;   "Do a lazy reduce, returning the original (lazy) sequence, 
;;   and passing the final accumulator value to `store-result!` 
;;   when the sequence is fully realized."
;;   [f seq]
;;   (loop [current-seq seq accum (dtt/->tensor (repeatedly 1e4 (fn [] 0)))]
;;     (if (empty? current-seq)
;;       accum
;;       (recur (next current-seq) (f accum (first current-seq))))))

;; (defn lang-profiling [seq] (imperative-reduce vb/bundle-op seq))
;; (defn dummy-fn [x] (println (count x)) x)

(doseq [filePath (serializer/list-files "trainingData/")]
  (let [lang (str  "lang/" (extract-file filePath))]
    (when (nil? (vb/get-hdv lang))
      (tufte/profile {} (p :lang
                           (let [n-grams (doall (->>
                                                 (serializer/lazy-file-lines filePath)
                                                 (remove clojure.string/blank?)
                                                 (pmap text-profiling)
                                                 (filter some?)))]
                             (serializer/serialize-negative-vector-as-string
                              (lazy-seq [[lang
                                          (dtt/->tensor
                                           (vb/clip
                                            (apply dtype-fn/+ n-grams))
                                           :container-type :native-heap
                                           :resource-type nil
                                           :datatype :byte)]]) "lang.vsa")))))))
(def lang-cleanup-mem (atom {}))
(seed/load-seed "lang.vsa" lang-cleanup-mem)

(doseq [filePath (serializer/list-files "testing/")]
  (let [lang (str  "lang/" (extract-file filePath))
        results
        (->> (serializer/lazy-file-lines filePath)
             (remove clojure.string/blank?)
             (map text-profiling)
             (filter some?)
             (map #(vb/query-cleanup-mem lang-cleanup-mem %))
             (map first))
        correctness
        (->> results (filter #(= lang %))
             (count))]
    (println (str "score for " lang " is " (float (/ correctness (count results))) " correctness " correctness " tests " (count results)))))

(def query (vb/bind (vb/bind (vb/protect-n (vb/get-hdv "t") 2) (vb/protect-n (vb/get-hdv "h") 1)) (vb/get-hdv "lang/eng")))
(def lang-query-dan (text-profiling "den foerste valgmulighed at tage konventionernes indhold op til fornyet overvejelse for at fylde alle hullerne ud er en kompliceret og risikopraeget mulighed kompliceret fordi krigsfoerelsen til stadighed skifter karakter og risikopraeget fordi det ikke kan udelukkes at aabningen af konventionerne for nye forhandlinger kan foere til at der ikke opnaas nogen ny enighed"))
(def lang-query-est (text-profiling "eesistujariik alustab toeoed euroopa liidu jaoks kriitilisel ajal ja ma soovin talle edu samas tunnistan et mul on ka moningaid kartusi "))
(def lang-query-eng (text-profiling "for many europeans a hens egg is a welcome part of their breakfast 
"))
(def lang-query-fra (text-profiling "en attenuant la valeur limite journaliere ou en adoptant des mesures de lutte contre le probleme de la pollution"))
(def lang-query-ell (text-profiling "den arkei na thespisei e  austerous kanones gia tis ekpompes dioxeidiou tou anthraka"))
(def lang-query-pol (text-profiling "innymi slowy finansowanie badan i rozwoju z ramienia rzadow europejskich jest ograniczone splacalne i w zaden sposob nie wplywa na konkurencyjnosc boeinga"))
(def lang-query-nld (text-profiling "en als we de economische samenwerking willen versterken moeten we natuurlijk onze inspanningen opvoeren om ons doel van een vrijhandelszone in  te bereiken"))

(vb/reset-hdv-mem!)

;(seed/load-seed "seed.vsa")

;(println (vb/query-cleanup-mem-verbose query))

(vb/reset-hdv-mem!)

(seed/load-seed "lang.vsa")

(println (vb/query-cleanup-mem lang-cleanup-mem (dtt/->tensor
                                                 (vb/clip lang-query-dan) :datatype :byte)))
(println (vb/query-cleanup-mem lang-cleanup-mem lang-query-est))
(println (vb/query-cleanup-mem lang-cleanup-mem lang-query-eng))
(println (vb/query-cleanup-mem lang-cleanup-mem lang-query-fra))
(println (vb/query-cleanup-mem lang-cleanup-mem lang-query-ell))
(println (vb/query-cleanup-mem lang-cleanup-mem lang-query-pol))
(println (first (vb/query-cleanup-mem lang-cleanup-mem lang-query-nld)))






