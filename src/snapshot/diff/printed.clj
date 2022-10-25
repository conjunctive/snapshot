(ns snapshot.diff.printed
  "Compute diffs between snapshots via `lambdaisland.deep-diff2/diff`.
  Intended for visualizing human-readable diffs at the REPL.

  `pprint` pretty-prints a human-readable difference between in-memory snapshots.

  `pprint!` pretty-prints a human-readable difference between snapshots.
  A snapshot is read from the filesystem, compared in entirety to the
  in-memory data."
  (:require [clojure.java.io :as io]
            [lambdaisland.deep-diff2]
            [net.cgrand.xforms.io :as xio]))

(defn pprint [snapshot current]
  (lambdaisland.deep-diff2/pretty-print
   (lambdaisland.deep-diff2/diff
    (vec snapshot)
    current)))

(defn pprint!
  [snapshot current]
  (pprint (xio/edn-in (io/resource snapshot)) current))
