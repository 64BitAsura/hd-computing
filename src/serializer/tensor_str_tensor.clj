(ns serializer.tensor-str-tensor (:require [clojure.string :as str]
                                           [tech.v3.datatype :as dtype]
                                           [tech.v3.tensor :as dtt]))

(defn seq-to-bits [s]
  (apply str (mapcat #(case %
                        -1 "11"
                        1 "10"
                        0 "00") s)))

(defn bits-to-bytes [bits]
  (map #(unchecked-byte (Integer/parseInt (apply str %) 2))
       (partition 8 8 nil bits)))

(defn bytes-to-str [bytes]
  (String. (byte-array bytes) "ISO-8859-1"))

(defn str-to-bytes [s]
  (.getBytes s "ISO-8859-1"))

(defn bytes-to-bits [bytes]
  (apply str (mapcat #(str/reverse (subs (str/reverse (Integer/toBinaryString %)) 0 (if (> (count (Integer/toBinaryString %)) 8) 8 (count (Integer/toBinaryString %)))))
                     bytes)))

(defn bits-to-seq [bits]
  (map (fn [b]
         (case (apply str b)
           "11" -1
           "10" 1
           "00" 0))
       (partition 2 2 bits)))
(defn log [content] content)

(defn convert-seq [s]
  (-> s seq-to-bits  bits-to-bytes  bytes-to-str))

(defn convert-back [s]
  (-> s log str-to-bytes  bytes-to-bits  bits-to-seq))

(defn convert-negative-vector-to-str [tensor] (convert-seq (dtype/->byte-array tensor)))

(defn convert-str-to-negative-vector [string] (dtt/->tensor (convert-back string)))


