(ns snapshot.core
  (:require [clojure.java.io :as io]
            [net.cgrand.xforms.io :as xio]))

(defn write!
  [file data]
  (xio/edn-out (io/resource file)
               identity
               data
               :append false))

(defn read!
  [file]
  (xio/edn-in (io/resource file)))
