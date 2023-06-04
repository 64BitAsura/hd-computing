(ns serializer.serial
  (:require [clojure.java.io :as io :refer [file output-stream input-stream]]))

(import [java.io ByteArrayOutputStream ObjectOutputStream ByteArrayInputStream ObjectInputStream])

(defn to-bytes [map]
  (let [baos (ByteArrayOutputStream.)
        oos (ObjectOutputStream. baos)]
    (.writeObject oos map)
    (.flush oos)
    (.close oos)
    (.toByteArray baos)))

(defn serialize-bytes [filename bytes]
  (with-open
   [out (output-stream
         (file filename) :append true)]
    (.write out bytes)
    (.write out (to-bytes (str "\n")))
    (.flush out)
    (.close out))
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
                                      [k (value-transformer  v)]))]
    (reset! @atom-object transformed-values)))

(defn increment [x]
  (println x) (+ x 1))

(defn to-bytes-in-steps [[k v]]
  (let [baos (ByteArrayOutputStream.)
        oos (ObjectOutputStream. baos)]
    (.writeObject oos k)
    (.writeObject oos v)
    (.flush oos); Flush after processing each entry
    (.close oos)
    (.toByteArray baos)))

(defn map-entries-lazy-seq [m]
  (lazy-seq (map (fn [[k v]] [k v]) m)))

(defn process-lazy-seq-one-at-a-time [lazySeq callback]
  (loop [current-lazySeq lazySeq]
    (when (seq current-lazySeq)
      (if (callback (first current-lazySeq))
        (recur (next current-lazySeq))))))

(defn bytes-to-map [bytes]
  (let [bais (ByteArrayInputStream. bytes)
        ois (ObjectInputStream. bais)
        deserialized-object (.readObject ois)]
    (if (= (type deserialized-object) clojure.lang.PersistentArrayMap)
      deserialized-object
      (throw (ex-info "Deserialized object is not of type clojure.lang.PersistentArrayMap" {:object deserialized-object})))))

(defn read-lines [filename onLine]
  (println filename)
  (with-open [rdr (io/reader filename)]
    ;;(doseq [line  (line-seq rdr)]
    (onLine (first (line-seq rdr)))))

(defn list-files [dir]
  (let [dir-file (java.io.File. dir)]
    (vec (map #(.getPath (java.io.File. dir (.getName %)))
              (filter #(-> % .isFile) (file-seq dir-file))))))

