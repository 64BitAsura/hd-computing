(ns serializer.tensor-str-tensor.test (:require
                                       [tech.v3.tensor :as dtt]
                                       [clojure.data.json :as json]
                                       [serializer.tensor-str-tensor]
                                       [gigasquid.vsa-base :as vb]
                                       [tech.v3.datatype.functional :as dtype-fn]
                                       [clojure.string]))
(use '[clojure.string :only (trim split)])

;; (def tensor (dtt/->tensor (repeatedly 10e4 #(if (> (rand) 0.5) -1
;;                                                 (if (> (rand) 0.5) 1 0))) :container-type :native-heap :resource-type nil :datatype :byte))

;; (def converted-str (json/write-str (serializer.tensor-str-tensor/convert-negative-vector-to-str tensor) :escape-unicode false :escape-js-separators false :escape-slash false))
;; (def back-seq (serializer.tensor-str-tensor/convert-str-to-negative-vector (json/read-str converted-str)))

;; (println "Input sequence:" tensor)
;; (println "Converted string:" converted-str)
;; (println "Back to sequence:" back-seq)
;; (println "compare" (compare tensor back-seq))

;; (println (dtt/->tensor  (vb/clip (vb/bundle-op (vb/bundle-op
;;                                                 (dtt/->tensor [-1 -1 2] :datatype :byte)
;;                                                 (dtt/->tensor [-1 -1 126] :datatype :byte)) (dtt/->tensor [-1 -1 126] :datatype :byte))) :datatype :byte))

;; (dorun (->>  (split "bantu - christian, orthodox - shia - neohinduism" #"-") (map println)))

(defn tfn [] (dtt/->tensor (repeatedly 10e4 #(if (> (rand) 0.5) -1
                                                 (if (> (rand) 0.5) 1 0))) :container-type :native-heap :resource-type nil :datatype :byte))

(def latin-alphabet (map str (into [] (concat (into [] (concat (map char (range (int \a) (inc (int \z)))) " "))))))

(def numeric (map str (into [] (concat  (range (int 0) (inc (int 200)))))))

(def box (atom {}))
(def pos (atom {}))
(defn add-box [key] (swap! box merge {key (tfn)}))
(defn add-pos [key] (swap! pos merge {key (tfn)}))
(run! add-box latin-alphabet)
(run! add-pos numeric)

(def bundle (apply dtype-fn/+ [(dtype-fn/* (get @box "a") (get @pos "1"))
                               (dtype-fn/* (get @box "b") (get @pos "2"))
                               (dtype-fn/* (get @box "c") (get @pos "3"))
                               (dtype-fn/* (get @box "d") (get @pos "4"))
                               (dtype-fn/* (get @box "f") (get @pos "5"))
                               (dtype-fn/* (get @box "e") (get @pos "6"))
                               (dtype-fn/* (get @box "g") (get @pos "7"))
                               (dtype-fn/* (get @box "h") (get @pos "8"))
                               (dtype-fn/* (get @box "a") (get @pos "7"))
                               (dtype-fn/* (get @box "i") (get @pos "9"))]))

(def query (apply dtype-fn/+ [(get @box "a") (get @box "h") (get @box "c") (get @box "i")]))

(def resultant (dtype-fn/* bundle query))

(println (vb/query-cleanup-mem 0.1 true box resultant))




