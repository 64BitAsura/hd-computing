(require '[clojure.java.io :as io])

(defn serialize-atom [atom filename]
  (binding [*print-dup* true]
    (with-open [writer (io/writer filename)]
      (print-dup @atom writer))))

(defn deserialize-atom [atom filename]
  (reset! atom (read-string (slurp filename))))

;; Define a Clojure atom with a map as its value
(def my-atom (atom {:key1 "value1" :key2 "value2"}))

;; Serialize the atom to a file
(serialize-atom my-atom "atom-map.ser")

(reset! my-atom {})

(print my-atom)
;; Deserialize the atom from the file
(deserialize-atom my-atom "atom-map.ser")

(print my-atom)
