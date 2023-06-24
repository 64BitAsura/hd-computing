(ns words
  (:require
   [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
   [tech.v3.tensor :as dtt]
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

(println (vb/get-hdv "lang/eng"))

(println "training")

(defn trigrams [text] (map (fn ([pos] (subs text pos (+ pos 3)))) (range 0 (- (count text) 2))))
(defn extract-file [s]
  (second (re-find #"trainingData/(.+?)\.txt" s)))

(defn langProfiling [lang-hdv line] (reduce                                                                                                 (fn [acc gram]
                                                                                                                                              (dtt/->tensor
                                                                                                                                               (if (nil? acc)
                                                                                                                                                 (vb/get-hdv gram)
                                                                                                                                                 (vb/bundle (vb/get-hdv gram) acc))))
                                                                                                                                            lang-hdv
                                                                                                                                            (trigrams  (trim line))))
(doseq [filePath (serializer/list-files "trainingData/")]
  (let [lang (str  "lang/" (extract-file filePath))]
    (when (nil? (vb/get-hdv lang))
      (tufte/profile {} (p :lang
                           (serializer/serialize-negative-vector-as-string
                            (lazy-seq [[lang
                                        (vb/add-hdv! lang
                                                     (reduce
                                                      #(langProfiling %1 %2)
                                                      nil
                                                      (take 10 (serializer/lazy-file-lines filePath))))]]) "seed.vsa"))))))

(def eng (vb/get-hdv "lang/eng"))
(def fin (vb/get-hdv "lang/fin"))

(println eng fin)

(println "resetting")

(vb/reset-hdv-mem!)

(trigram/warmup-trigrams-from-seed (seed/seed))

(println (compare eng  (vb/get-hdv "lang/eng")))
(println (compare fin (vb/get-hdv "lang/fin")))





