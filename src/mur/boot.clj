(ns mur.boot
  (:require
   [boot.core :as bt]
   [boot.util :as btu]
   [clojure.tools.namespace.dir :as nsdir]
   [clojure.tools.namespace.track :as nstrk]
   [mur.repl :as rpl]))

(defn- restart-system?
  [system-symbol prev-fileset next-fileset files {:keys [paths regexes]}]
  (when (some? system-symbol)
    (if (empty? files)
      (btu/info (str "No `files` to be watched."
                     "Will not attempt to manage `system`'s lifecycles.\n"))
      (let [input-files (bt/input-files
                         (bt/fileset-diff prev-fileset next-fileset))
            query-fn    (cond
                          paths
                          bt/by-path

                          regexes
                          bt/by-re

                          :else
                          bt/by-name)
            files (if (some? regexes)
                    (into [] (map re-pattern) files)
                    files)]
        (->> input-files
             (query-fn files)
             (seq)
             (boolean))))))

(defn- validate-option
  [{:keys [paths regexes]}]
  (when (and (true? paths) (true? regexes))
    (btu/fail "Cannot specify both --paths and --regexes to `true`")
    (throw (ex-info "Incorrect option" {}))))

(bt/deftask system
  [s system VAL  sym   "Symbol of the system. Should point to a non-arity fn"
   f files  VALS [str] "Files to be watched for restarting system. Name-based."
   p paths       bool  "`files` as path. Pick between this and `regexes`"
   r regexes     bool  "`files` as regex. Pick between this and `paths`"]
  (validate-option *opts*)
  (let [prev-fileset_ (atom nil)
        dirs          (into [] (bt/get-env :directories))
        tracker_      (atom (nsdir/scan-dirs (nstrk/tracker) dirs))
        init-system   (if (some? system)
                        (delay
                         (btu/info "Starting system...\n")
                         (rpl/setup! system)
                         (rpl/boot!))
                        (delay
                         (btu/info (str "No system is supplied."
                                        "Will only refresh namespaces.\n"))))]
    (fn [next-task]
      (fn [fileset]
        (when (realized? init-system)
          (swap! tracker_ nsdir/scan-dirs)
          (rpl/reboot! @tracker_ (restart-system? system
                                                  @prev-fileset_
                                                  fileset
                                                  files
                                                  {:paths   paths
                                                   :regexes regexes})))
        @init-system
        (next-task (reset! prev-fileset_ fileset))))))
