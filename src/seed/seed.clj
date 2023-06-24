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

(defn load-seed [] (run! #(vb/add-hdv! (get % 0) (get % 1))
                         (map serializer/decode-json
                              (serializer/loadVSA "seed.vsa"))))

(defn seed []
  (if (serializer/file-exists? "seed.vsa")
    (load-seed)
    (create-seed)) latin-alphabet)

