(ns seed.seed (:require [gigasquid.vsa-base :as vb]
                        [serializer.serial :as serializer]))

(def latin-alphabet (map str (into [] (concat (into [] (concat (map char (range (int \a) (inc (int \z)))) " "))))))

(defn create-seed []
  (println "creating seed data")
  (run! vb/add-hdv! latin-alphabet)
  (println "serialize seed vector")
  (serializer/serialize-negative-vector-as-string
   (serializer/map-entries-lazy-seq @vb/cleanup-mem)
   "seed.vsa"))

(defn load-seed ([filename cleanup-mem] (run! #(vb/add-hdv! (get % 0) (get % 1) cleanup-mem)
                                              (map serializer/decode-json
                                                   (serializer/loadVSA filename)))) ([filename] (load-seed filename vb/cleanup-mem)))

(defn seed []
  (if (serializer/file-exists? "seed.vsa")
    (load-seed "seed.vsa")
    (create-seed)) latin-alphabet)

