(ns serializer.tensor-str-tensor.test (:require
                                       [tech.v3.tensor :as dtt]
                                       [serializer.tensor-str-tensor]
                                       [clojure.string]))

(def tensor (dtt/->tensor (repeatedly 200 #(if (> (rand) 0.5) -1
                                               (if (> (rand) 0.5) 1 0))) :container-type :native-heap :resource-type nil :datatype :byte))

(def converted-str (serializer.tensor-str-tensor/convert-negative-vector-to-str tensor))
(def back-seq (serializer.tensor-str-tensor/convert-str-to-negative-vector converted-str))

(println "Input sequence:" tensor)
(println "Converted string:" converted-str)
(println "Back to sequence:" back-seq)


