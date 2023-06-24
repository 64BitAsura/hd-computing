(ns serializer.tensor-str-tensor (:require [clojure.string :as str]
                                           [tech.v3.datatype :as dtype]
                                           [clojure.pprint :as pp]
                                           [tech.v3.tensor :as dtt]))

(defn seq-to-bits [s]
  (apply str (mapcat #(case %
                        -1 "11"
                        1 "10"
                        0 "00") s)))

(defn unsigned-binary-8 [n]
  (pp/cl-format nil "~8,'0B" n))

(defn signed-binary-8 [n]
  (unsigned-binary-8 (bit-and n 0xff)))

(defn bytes-to-unchecked [bytes] (map unchecked-byte bytes))

(defn bits-to-bytes [bits]
  (map #(Integer/parseInt (apply str %) 2)
       (partition 8 8 nil bits)))

(defn bytes-to-str [bytes]
  (String. (byte-array bytes) "ISO-8859-1"))

(defn str-to-bytes [s]
  (map #(bit-and % 0xff) (.getBytes s "ISO-8859-1")))

(defn bytes-to-bits [bytes]
  (apply str (mapcat #(unsigned-binary-8 %)
                     bytes)))

(defn log [x] (println x) x)

(defn bits-to-seq [bits]
  (doall (map (fn [b]
                (case (apply str b)
                  "11" -1
                  "10" 1
                  "00" 0))
              (partition 2 2 bits))))

(defn convert-seq [s]
  (-> s seq-to-bits bits-to-bytes bytes-to-unchecked bytes-to-str))

(defn convert-back [s]
  (-> s str-to-bytes bytes-to-bits  bits-to-seq))

(defn convert-negative-vector-to-str [tensor] (convert-seq (dtype/->byte-array tensor)))

(defn convert-str-to-negative-vector [string] (dtt/->tensor (convert-back string) :container-type :native-heap :resource-type nil :datatype :byte))


