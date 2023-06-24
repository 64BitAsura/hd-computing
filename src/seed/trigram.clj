(ns seed.trigram (:require [gigasquid.vsa-base :as vb]
                           [tech.v3.tensor :as dtt]))

(defn warmup-trigrams-from-seed [chars]
  (dorun (for [c1 chars
               c2 chars
               c3 chars]
           (when (nil? (vb/get-hdv (str c1 c2 c3)))
             (vb/add-hdv!
              (str c1 c2 c3)
              (dtt/->tensor (vb/bind
                             (dtt/->tensor
                              (vb/bind
                               (vb/get-hdv c1)
                               (vb/protect-n
                                (vb/get-hdv c2) 1))
                              :container-type :native-heap
                              :resource-type nil
                              :datatype :byte)
                             (vb/protect-n (vb/get-hdv c3) 2))
                            :container-type :native-heap
                            :resource-type nil
                            :datatype :byte))))))