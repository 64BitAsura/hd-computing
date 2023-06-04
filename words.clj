(ns words
  (:require
   [tech.v3.tensor :as dtt]
   [tech.v3.datatype :as dtype]
   [serializer.serial :as serializer]
   [gigasquid.vsa-data :as vd]
   [gigasquid.vsa-base :as vb]))

;; First thing is that we need a global memory to remember all of our hdv
(vb/reset-hdv-mem!)

;; Generate a sequence of Latin alphabet characters
(def latin-alphabet (into [] (concat (into [] (concat (map char (range (int \a) (inc (int \z)))) " ")))))

(run! vb/add-hdv! latin-alphabet)

;; ;; Function to generate all possible trigrams from a given sequence of characters
(defn generate-trigrams [chars]
  (dorun (for [c1 chars
               c2 chars
               c3 chars]
           (if (nil? (vb/get-hdv (str (char c1) (char c2) (char c3))))  (vb/add-hdv!
                                                                         (str (char c1) (char c2) (char c3))
                                                                         (dtt/->tensor (vb/bind
                                                                                        (dtt/->tensor (vb/bind  (vb/get-hdv c1)  (vb/protect-n (vb/get-hdv c2) 1)))
                                                                                        (vb/protect-n (vb/get-hdv c3) 2)))) (println "exists")))))

(println "trigramming")
;; Call the function with the sequence of Latin alphabet characters
(generate-trigrams latin-alphabet)

(println "training")
(use '[clojure.string :only (trim)])
(defn trigrams [text] (map (fn ([pos] (subs text pos (+ pos 3)))) (range 0 (- (count text) 2))))
(defn extract-file [s]
  (second (re-find #"trainingData/(.+?)\.txt" s)))

(defn langProfiling [line lang] (reduce
                                 (fn [acc gram]
                                   (vb/add-hdv! lang (dtt/->tensor (vb/bundle (vb/get-hdv gram) (if (string? acc) (vb/get-hdv acc) acc)))))
                                 (trigrams  (trim line))))
(doseq [filePath (serializer/list-files "trainingData/")]
  (serializer/read-lines filePath (fn ([line]
                                       (langProfiling line (str (extract-file filePath) "-lang"))))))

(println (vb/get-hdv "eng"))
(println (count @vb/cleanup-mem))
(println "serializing!!!")
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
             (serializer/to-bytes-in-steps)
             (serializer/serialize-bytes "model.vsa") (Truce))))))

