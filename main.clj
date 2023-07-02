(ns main
  (:require
   [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
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

(defn extract-file [s]
  (second (re-find #"trainingData/(.+?)\.txt" s)))
(defn zero [] 0)
(defn lang-profiling [line] (reduce
                             #(dtt/->tensor
                               (vb/bundle-op
                                (or (vb/get-hdv %1) %1) (vb/get-hdv %2))
                               :container-type
                               :jvm-heap
                               :resource-type
                               :gc
                               :datatype
                               :int)
                             (trigrams (trim line))))

(doseq [filePath (serializer/list-files "trainingData/")]
  (let [lang (str  "lang/" (extract-file filePath))]
    (when (nil? (vb/get-hdv lang))
      (tufte/profile {} (p :lang
                           (serializer/serialize-negative-vector-as-string
                            (lazy-seq [[lang
                                        (dtt/->tensor
                                         (vb/clip
                                          (->>
                                           (serializer/lazy-file-lines filePath)
                                           (pmap lang-profiling)
                                           (reduce vb/bundle-op)))
                                         :container-type :native-heap
                                         :resource-type nil
                                         :datatype :byte)]]) "lang.vsa"))))))

(seed/load-seed "lang.vsa")
(def query (vb/bind (vb/bind (vb/protect-n (vb/get-hdv "t") 2) (vb/protect-n (vb/get-hdv "h") 1)) (vb/get-hdv "lang/eng")))
(def lang-query (lang-profiling "den foerste valgmulighed at tage konventionernes indhold op til fornyet overvejelse for at fylde alle hullerne ud er en kompliceret og risikopraeget mulighed kompliceret fordi krigsfoerelsen til stadighed skifter karakter og risikopraeget fordi det ikke kan udelukkes at aabningen af konventionerne for nye forhandlinger kan foere til at der ikke opnaas nogen ny enighed"))
(def lang-query2 (lang-profiling "eesistujariik alustab toeoed euroopa liidu jaoks kriitilisel ajal ja ma soovin talle edu samas tunnistan et mul on ka moningaid kartusi "))
(def lang-query3 (lang-profiling "for many europeans a hens egg is a welcome part of their breakfast 
"))

(vb/reset-hdv-mem!)

(seed/load-seed "seed.vsa")

(println (vb/query-cleanup-mem-verbose query))

(vb/reset-hdv-mem!)

(seed/load-seed "lang.vsa")

(println (vb/query-cleanup-mem lang-query))
(println (vb/query-cleanup-mem lang-query2))
(println (vb/query-cleanup-mem lang-query3))






