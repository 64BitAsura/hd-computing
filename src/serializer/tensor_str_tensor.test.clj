(ns serializer.tensor-str-tensor.test (:require
                                       [tech.v3.tensor :as dtt]
                                       [clojure.data.json :as json]
                                       [serializer.tensor-str-tensor]
                                       [clojure.string]
                                       [seed.seed :as seed]
                                       [seed.trigram :as trigram]
                                       [gigasquid.vsa-base :as vb]))
(use '[clojure.string :only (trim split)])

(def tensor (dtt/->tensor (repeatedly 16 #(if (> (rand) 0.5) -1
                                              (if (> (rand) 0.5) 1 0))) :container-type :native-heap :resource-type nil :datatype :byte))

(def converted-str (json/write-str (serializer.tensor-str-tensor/convert-negative-vector-to-str tensor) :escape-unicode false :escape-js-separators false :escape-slash false))
(def back-seq (serializer.tensor-str-tensor/convert-str-to-negative-vector (json/read-str converted-str)))

(println "Input sequence:" tensor)
(println "Converted string:" converted-str)
(println "Back to sequence:" back-seq)
(println "compare" (compare tensor back-seq))

(println (dtt/->tensor  (vb/clip (vb/bundle-op (vb/bundle-op
                                                (dtt/->tensor [-1 -1 2] :datatype :byte)
                                                (dtt/->tensor [-1 -1 126] :datatype :byte)) (dtt/->tensor [-1 -1 126] :datatype :byte))) :datatype :byte))

(dorun (->>  (split "bantu - christian, orthodox - shia - neohinduism" #"-") (map println)))

