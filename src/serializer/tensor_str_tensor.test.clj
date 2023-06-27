(ns serializer.tensor-str-tensor.test (:require
                                       [tech.v3.tensor :as dtt]
                                       [clojure.data.json :as json]
                                       [serializer.tensor-str-tensor]
                                       [clojure.string]))

(def tensor (dtt/->tensor (repeatedly 10000 #(if (> (rand) 0.5) -1
                                                 (if (> (rand) 0.5) 1 0))) :container-type :native-heap :resource-type nil :datatype :byte))

(def converted-str (json/write-str (serializer.tensor-str-tensor/convert-negative-vector-to-str tensor) :escape-unicode false :escape-js-separators false :escape-slash false))
(def back-seq (serializer.tensor-str-tensor/convert-str-to-negative-vector (json/read-str converted-str)))

(println "Input sequence:" tensor)
(println "Converted string:" converted-str)
(println "Back to sequence:" back-seq)
(println "compare" (compare tensor back-seq))


