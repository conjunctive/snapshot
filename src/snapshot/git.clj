(ns snapshot.git
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as git-query]
            [clj-jgit.internal :as git-]
            [net.cgrand.xforms.io :as xio])
  (:import (java.io PushbackReader)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib ObjectLoader Repository)))

(defn commit-msg->id!
  "From the current branch of the Git repository at `repo-path`,
  construct a mapping of commit messages to their corresponding identifiers.
  The optional `opts` argument is forwarded to `clj-jgit.porcelain/git-log`."
  {:inline-arities #{1}}
  ([repo-path] (commit-msg->id! repo-path nil))
  ([repo-path opts]
   (with-open [repo (git/load-repo repo-path)]
     (let [log (git/git-log repo opts)]
       (into {}
             (map (juxt :msg
                        (comp :id
                              (partial git-query/commit-info repo)
                              :id)))
             log)))))

(defn comitted!
  "Access a snapshot from a previous Git commit.
  Returns a reducible view over the stream of EDN
  contained within the snapshot at `file-path`,
  relative to the commit `commit-id`."
  {:inline-arities #{2}}
  ([commit-id file-path] (comitted! "." commit-id file-path))
  ([repo-path commit-id file-path]
   (with-open [repo (git/load-repo repo-path)
               rev-walk (git-/new-rev-walk repo)]
     (let [commit (git-/bound-commit repo rev-walk (git-/resolve-object commit-id repo))
           blob-id (git/get-blob-id repo commit file-path)]
       (xio/edn-in (.. repo
                       (Git/getRepository)
                       (Repository/open blob-id)
                       (ObjectLoader/getBytes)))))))

(comment
  ;; Locate the Git repository.
  (def repo-path ".")

  ;; Decide on a commit from the current branch.
  (def commit-msg->id (commit-msg->id! repo-path))
  (keys commit-msg->id)
  (def commit-id (get commit-msg->id "..."))

  ;; Stream the snapshot from the specified commit into a vector.
  (vec (committed! repo-path commit-id "resources/snapshot/current.edn"))
  ,)
