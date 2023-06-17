(ns words
  (:require
   [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
   [tech.v3.tensor :as dtt]
   [tech.v3.datatype :as dtype]
   [serializer.serial :as serializer]
   [gigasquid.vsa-data :as vd]
   [clojure.data.codec.base64 :as b64]
   [gigasquid.vsa-base :as vb]
   [tech.v3.datatype.functional :as dtype-fn]))

(tufte/add-basic-println-handler! {})
(use '[clojure.string :only (trim)])
;; First thing is that we need a global memory to remember all of our hdv
(vb/reset-hdv-mem!)
;; Generate a sequence of Latin alphabet characters
(def latin-alphabet (map str (into [] (concat (into [] (concat (map char (range (int \a) (inc (int \z)))) " "))))))
(println latin-alphabet)
(defn source []
  ;; ;; Function to generate all possible trigrams from a given sequence of characters
  (defn generate-trigrams [chars]
    (dorun (for [c1 chars
                 c2 chars
                 c3 chars]
             (if (nil? (vb/get-hdv (str c1 c2 c3)))
               (vb/add-hdv!
                (str c1 c2 c3)
                (dtt/->tensor (vb/bind
                               (dtt/->tensor (vb/bind  (vb/get-hdv c1)  (vb/protect-n (vb/get-hdv c2) 1)))
                               (vb/protect-n (vb/get-hdv c3) 2)))) (println "exists")))))

  (println "trigramming")
  (run! vb/add-hdv! latin-alphabet)
;; Call the function with the sequence of Latin alphabet characters
  (tufte/profile {} (p :trigramming (generate-trigrams latin-alphabet)))

  (println "trigram serializing!!!")
  (def byted
    (serializer/transform-values @vb/cleanup-mem dtype/->byte-array))
  (defn Truce ([x] true))
  (serializer/process-lazy-seq-one-at-a-time
   (serializer/map-entries-lazy-seq
    byted)
   (fn ([[k v]]
        (if (or (nil? k) (nil? v))
          false
          (->> [k v]
               (serializer/serializeJson)
               (serializer/to-bytes-in-steps)
               (serializer/serialize-bytes "trigram.vsa") (Truce)))))))

(if (serializer/file-exists? "trigram.vsa") (serializer/read-lines "trigram.vsa" (fn ([line] (->> line (serializer/deserializeJson) (fn [vector] (vb/add-hdv! (get vector 0) (dtt/->tensor (get vector 1)))))))) (source))

(println (vb/get-hdv "acb"))

(println "training")

(defn trigrams [text] (map (fn ([pos] (subs text pos (+ pos 3)))) (range 0 (- (count text) 2))))
(defn extract-file [s]
  (second (re-find #"trainingData/(.+?)\.txt" s)))

(defn langProfiling [line lang] (vb/add-hdv! lang (reduce
                                                   (fn [acc gram]
                                                     (dtt/->tensor
                                                      (if (nil? acc)
                                                        (vb/get-hdv gram)
                                                        (vb/bundle (vb/get-hdv gram) acc))))
                                                   (vb/get-hdv lang)
                                                   (trigrams  (trim line)))))
(doseq [filePath (serializer/list-files "trainingData/")]
  (tufte/profile {} (p :lang (serializer/read-lines filePath (fn ([line]
                                                                  (p :line (langProfiling line (str (extract-file filePath) "-lang")))))))))

;; (println (vb/get-hdv "eng"))
(println (count @vb/cleanup-mem))


