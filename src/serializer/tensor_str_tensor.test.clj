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

(def numeric (map str (into [] (concat  (range (int 0) (inc (int 20)))))))

(def box (atom {}))
(def pos (atom {}))
(defn add-box [key] (swap! box merge {key (tfn)}))
(defn add-pos [key] (swap! pos merge {key (tfn)}))
(run! add-box latin-alphabet)
(run! add-pos numeric)

(def query (apply dtype-fn/+ [(dtype-fn/*
                               (get @box "h")
                               (vb/protect-n (get @box "e") 1)
                               (vb/protect-n (get @box "l") 2)
                               (get @pos "1"))
                              (dtype-fn/*
                               (get @box "e")
                               (vb/protect-n (get @box "l") 1)
                               (vb/protect-n (get @box "l") 2)
                               (get @pos "2"))
                              (dtype-fn/*
                               (get @box "l")
                               (vb/protect-n (get @box "l") 1)
                               (vb/protect-n (get @box "o") 2)
                               (get @pos "3"))
                              (dtype-fn/*
                               (get @box "l")
                               (vb/protect-n (get @box "o") 1)
                               (vb/protect-n (get @box " ") 2)
                               (get @pos "4"))
                              (dtype-fn/*
                               (get @box "o")
                               (vb/protect-n (get @box " ") 1)
                               (vb/protect-n (get @box "w") 2)
                               (get @pos "5"))
                              (dtype-fn/*
                               (get @box " ")
                               (vb/protect-n (get @box "w") 1)
                               (vb/protect-n (get @box "o") 2)
                               (get @pos "6"))
                              (dtype-fn/*
                               (get @box "w")
                               (vb/protect-n (get @box "o") 1)
                               (vb/protect-n (get @box "r") 2)
                               (get @pos "7"))
                              (dtype-fn/*
                               (get @box "o")
                               (vb/protect-n (get @box "r") 1)
                               (vb/protect-n (get @box "l") 2)
                               (get @pos "8"))
                              (dtype-fn/*
                               (get @box "r")
                               (vb/protect-n (get @box "l") 1)
                               (vb/protect-n (get @box "d") 2)
                               (get @pos "9"))
                              (dtype-fn/*
                               (get @box "l")
                               (vb/protect-n (get @box "d") 1)
                               (vb/protect-n (get @box " ") 2)
                               (get @pos "10"))
                              (dtype-fn/*
                               (get @box "d")
                               (vb/protect-n (get @box " ") 1)
                               (vb/protect-n (get @box "s") 2)
                               (get @pos "11"))
                              (dtype-fn/*
                               (get @box " ")
                               (vb/protect-n (get @box "s") 1)
                               (vb/protect-n (get @box "a") 2)
                               (get @pos "12"))
                              (dtype-fn/*
                               (get @box "s")
                               (vb/protect-n (get @box "a") 1)
                               (vb/protect-n (get @box "m") 2)
                               (get @pos "13"))]))

(def profile (apply dtype-fn/+ [(dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "h") 1)
                                 (vb/protect-n (get @box "e") 2))
                                (dtype-fn/*
                                 (get @box "h")
                                 (vb/protect-n (get @box "e") 1)
                                 (vb/protect-n (get @box "l") 2))
                                (dtype-fn/*
                                 (get @box "e")
                                 (vb/protect-n (get @box "l") 1)
                                 (vb/protect-n (get @box "l") 2))
                                (dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "b") 1)
                                 (vb/protect-n (get @box "a") 2))
                                (dtype-fn/*
                                 (get @box "b")
                                 (vb/protect-n (get @box "a") 1)
                                 (vb/protect-n (get @box "d") 2))
                                (dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "u") 1)
                                 (vb/protect-n (get @box "g") 2))
                                (dtype-fn/*
                                 (get @box "u")
                                 (vb/protect-n (get @box "g") 1)
                                 (vb/protect-n (get @box "l") 2))
                                (dtype-fn/*
                                 (get @box "g")
                                 (vb/protect-n (get @box "l") 1)
                                 (vb/protect-n (get @box "y") 2))
                                (dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "d") 1)
                                 (vb/protect-n (get @box "o") 2))
                                (dtype-fn/*
                                 (get @box "d")
                                 (vb/protect-n (get @box "o") 1)
                                 (vb/protect-n (get @box "o") 2))
                                (dtype-fn/*
                                 (get @box "o")
                                 (vb/protect-n (get @box "o") 1)
                                 (vb/protect-n (get @box "m") 2))
                                (dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "c") 1)
                                 (vb/protect-n (get @box "h") 2))
                                (dtype-fn/*
                                 (get @box "c")
                                 (vb/protect-n (get @box "h") 1)
                                 (vb/protect-n (get @box "a") 2))
                                (dtype-fn/*
                                 (get @box "h")
                                 (vb/protect-n (get @box "a") 1)
                                 (vb/protect-n (get @box "o") 2))
                                (dtype-fn/*
                                 (get @box "a")
                                 (vb/protect-n (get @box "o") 1)
                                 (vb/protect-n (get @box "s") 2))
                                (dtype-fn/*
                                 (get @box " ")
                                 (vb/protect-n (get @box "s") 1)
                                 (vb/protect-n (get @box "a") 2))
                                (dtype-fn/*
                                 (get @box "s")
                                 (vb/protect-n (get @box "a") 1)
                                 (vb/protect-n (get @box "m") 2))

                              ;; (dtype-fn/*
                              ;;  (get @box "r")
                              ;;  (vb/protect-n (get @box "l") 1)
                              ;;  (vb/protect-n (get @box "d") 2))
                                ]))

(def resultant (dtype-fn/* profile query))

(println (vb/query-cleanup-mem 0.003 true pos resultant))




