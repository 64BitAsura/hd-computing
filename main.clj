(ns main
  (:require
   [gigasquid.vsa-base :as vb]
   [gigasquid.vsa-data :as vd]
   [tech.v3.datatype :as dtype]
   [tech.v3.tensor :as dtt]
   [tech.v3.datatype.functional :as dtype-fn]
   [clj-commons.byte-streams :as bs]
   [clojure.java.io :as io :refer [file output-stream input-stream]]))

;; First thing is that we need a global memory to remember all of our hdv
;; (vb/reset-hdv-mem!)

;; @vb/cleanup-mem

;; (vb/reset-hdv-mem!)

;; (def primary-color-vsa-map (vd/clj->vsa {:x :red :y :yellow :z :blue}))

;; ;; let's add a new compound value to the cleanup memory that is green
;; (vb/add-hdv! :green (vb/bundle
;;                      (vb/get-hdv :yellow)
;;                      (vb/get-hdv :blue)))

;; ;; Now we can query the primary color map for something that is close to green. We get 2 results now.

;; (print (vd/vsa-get primary-color-vsa-map :green {:threshold 0.3}))

;; ;; =>  [{:z #tech.v3.tensor<int8>[1000000][1 -1 1 ... -1 1 1]}
;; ;;     {:y #tech.v3.tensor<int8>[1000000] [-1 1 1 ... 1 1 1]}]

;; ;; So we have a hdv of things, we can inspect it and compare it to our memory to see what's in it

;; (vd/vsa-inspect primary-color-vsa-map)

;; ;; Note that it includes green in it since it is a compound value
;; ;; =>  #{:y :yellow :green :z :red :blue :x}

;; (vd/vsa-inspect (vd/clj->vsa {:x :red}))

;; ;; =>  #{:red :x}

;; ;; Finally if we have a vector (stack) of these maps we can define map and filter functions on them

;; (def color-vsa-vector-map (vd/clj->vsa [{:x :yellow} {:x :green} {:z :red}]))

;; (vd/vsa-map #(->> (vd/vsa-get % :yellow {:threshold 0.01})
;;                   (mapv ffirst))
;;             color-vsa-vector-map)

;; ;; =>  ([:x] [:x] [])

;; (->> color-vsa-vector-map
;;      (vd/vsa-filter #(vd/vsa-get % :yellow {:threshold 0.01}))
;;      count)

(defn serialize-bytes [bytes filename]
  (with-open [out (output-stream (file filename))]
    (.write out bytes))
  filename)

(defn deserialize [filename]
  (with-open [in (input-stream (file filename))]
    (let [buf (byte-array (.available in))]
      (.read in buf)
      buf)))

(defn serialize-atom [atom filename]
  (binding [*print-dup* true]
    (with-open [writer (io/writer filename)]
      (print-dup @atom writer))))

(defn deserialize-atom [atom filename]
  (@atom (reset! @atom (read-string (slurp filename)))))

(defn convert-to-bytes [vector]
  (byte-array vector))

(defn- file-exists? [path]
  (.exists (io/file path)))

(defn transform-values [input-object value-transformer]
  (into {} (map (fn [[k v]] [k (value-transformer v)]) input-object)))

(defn transform-atom
  [atom-object value-transformer]
  (let [transformed-values (into {} (for [[k v] @atom-object]
                                      [k (value-transformer v)]))]
    (reset! @atom-object transformed-values)))

(defn increment [x]
  (println x) (+ x 1))

(import [java.io ByteArrayOutputStream ObjectOutputStream ByteArrayInputStream ObjectInputStream])

(defn to-bytes [map]
  (let [baos (ByteArrayOutputStream.)
        oos (ObjectOutputStream. baos)]
    (.writeObject oos map)
    (.flush oos)
    (.close oos)
    (.toByteArray baos)))

(defn bytes-to-map [bytes]
  (let [bais (ByteArrayInputStream. bytes)
        ois (ObjectInputStream. bais)
        deserialized-object (.readObject ois)]
    (if (= (type deserialized-object) clojure.lang.PersistentArrayMap)
      deserialized-object
      (throw (ex-info "Deserialized object is not of type clojure.lang.PersistentArrayMap" {:object deserialized-object})))))

;;(reset! at0m (transform-values vb/cleanup-mem dtt/tensor->buffer))

;;(if (file-exists? "model.vsa")
  ;;(print "model.vsa exists")

(vd/map->vsa {:x 1 :y 2})

(serialize-bytes (to-bytes
                  (transform-values @vb/cleanup-mem dtype/->byte-array)) "model.vsa")

(print (dtype-fn/equals (dtt/->tensor (get (bytes-to-map (deserialize "model.vsa")) :x)) (get @vb/cleanup-mem :x)))

;; (vb/reset-mem!)

;; (reset! vb/cleanup-mem (transform-values (deserialize "model.vsa") dtt/->tensor))

;; (println (vd/vsa-get vb/cleanup-mem :y))

;; (print (dtt/->tensor (deserialize (if (file-exists? "tensor.binary")
;;                                     (do (println "ran") (str "tensor.binary"))
;;                                     (serialize-bytes (convert-to-bytes (dtt/tensor->buffer (dtt/->tensor (range 10)))) "tensor.binary")))))

;; (serialize-atom vb/cleanup-mem "bytes-in-file.vds")

;; (vb/reset-mem!)

;; (def at0m (atom {}))

;; (deserialize-atom at0m "bytes-in-file.vds")
;; (print (vd/vsa-get at0m :y))


