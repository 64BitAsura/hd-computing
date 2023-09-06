(ns seed.seed (:require [gigasquid.vsa-base :as vb]
                        [serializer.serial :as serializer]))

(def latin-alphabet (map str (into [] (concat (into [] (concat (map char (range (int \a) (inc (int \z)))) " "))))))

(def numeric (map str (into [] (concat  (range (int 0) (inc (int 9)))))))

(def pos (map int (into [] (concat  (range (int 0) (inc (int 100)))))))

(def special-character '("," "/" "-" "_" "?" "(" ")" "[" "]" "." "'" "`" "/" "'" "\"" "&" "!" "@" "#" "$" "%" "^" "*" "{" "}" ":" ";" "<" ">" "|" "+" "=" "~"))

(println latin-alphabet special-character numeric pos)

(defn create-seed []
  (println "creating seed data")
  (run! vb/add-hdv! latin-alphabet)
  (run! vb/add-hdv! numeric)
  (run! vb/add-hdv! special-character)
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
    (create-seed))
  (map str (into []  (concat latin-alphabet numeric))))

