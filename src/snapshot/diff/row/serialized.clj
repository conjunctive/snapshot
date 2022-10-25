(ns snapshot.diff.row.serialized
  "Compute diffs between snapshots via `clojure.data/diff`.

  Intended primarily to be serialized to disk.
  For visualizing human-readable diffs at the REPL, see `snapshot.diff.printed`.

  Rows are items within a sequence, typically acquired from a stream of EDN.
  Comparisons operate on a row-by-row basis, which places a constraint on the input
  where the row count is expected to remain fairly static while the contents of said
  rows are expected to change frequently over time.

  `some-diff` returns the first difference from a row-by-row comparison of in-memory data.
  `some-diff!` returns the first difference from a row-by-row comparison,
  where the in-memory data is compared against data read from disk.

  `diff!` performs a row-by-row comparison, serializing all differences as an EDN stream to disk.
  During the comparison, in-memory data is compared against data read from disk."
  (:require [clojure.java.io :as io]
            [clojure.data]
            [net.cgrand.xforms :as x]
            [net.cgrand.xforms.io :as xio]))

(defn- diff-xf [b]
  (comp (map-indexed (fn [idx a-row]
                       (let [b-row (nth b idx nil)]
                         (clojure.data/diff a-row b-row))))
        (remove (fn [[only-a only-b in-both]]
                  (and (nil? only-a) (nil? only-b))))))

(defn some-diff
  "Compares rows between collections `snapshot` and `current` data.
  Returns the first computed diff between a row, otherwise `nil`."
  [snapshot current]
  (x/some (diff-xf (doall current)) snapshot))

(defn some-diff!
  "Compares rows between snapshot read from filesystem and `current` data.
   Returns the first computed diff between a row, otherwise `nil`. "
  [snapshot current]
  (some-diff (xio/edn-in (io/resource snapshot)) current))

(defn diff!
  [snapshot diff current]
  (xio/edn-out (io/resource diff)
               (diff-xf current)
               (xio/edn-in (io/resource snapshot))
               :append false))
