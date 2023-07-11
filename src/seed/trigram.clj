(ns seed.trigram (:require [gigasquid.vsa-base :as vb]
                           [tech.v3.tensor :as dtt]))

(defn trigram-dot-product [c1 c2 c3] (when (nil? (vb/get-hdv (str c1 c2 c3)))
                                       (vb/add-hdv!
                                        (str c1 c2 c3)
                                        (dtt/->tensor (vb/bind
                                                       (vb/bind
                                                        (vb/protect-n (vb/get-hdv c1) 2)
                                                        (vb/protect-n
                                                         (vb/get-hdv c2) 1))
                                                       (vb/get-hdv c3))))))

(defn warmup-trigrams-from-seed [chars]
  (dorun (for [c1 chars
               c2 chars
               c3 chars]
           (when (nil? (vb/get-hdv (str c1 c2 c3)))
             (vb/add-hdv!
              (str c1 c2 c3)
              (dtt/->tensor (vb/bind
                             (vb/bind
                              (vb/protect-n (vb/get-hdv c1) 2)
                              (vb/protect-n
                               (vb/get-hdv c2) 1))
                             (vb/get-hdv c3)))))))
  (println (count @vb/cleanup-mem)))