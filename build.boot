(set-env!
 :source-paths #{"src/"}
 :dependencies '[;; ---- clj ----
                 [org.clojure/clojure "1.10.0-alpha5"]
                 [http-kit "2.3.0" :scope "provided"]
                 [com.datomic/datomic-free "0.9.5697" :scope "provided"]
                 [io.rkn/conformity "0.5.1" :scope "provided"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 ;; ---- cljc ----
                 [com.stuartsierra/component "0.3.2"]
                 [bidi "2.1.3" :scope "provided"]
                 [bouncer "1.0.1" :scope "provided"]
                 [com.taoensso/timbre "4.10.0" :scope "provided"]
                 ;; ---- dev ----
                 [samestep/boot-refresh "0.1.0" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require
 '[samestep.boot-refresh :refer [refresh]]
 '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]])

(def +project-name+
  'mur)

(def +version+
  "0.1.9-SNAPSHOT")

(bootlaces! +version+)

(task-options!
 push {:ensure-branch nil
       :repo-map      {:checksum :warn}}
 pom  {:project     +project-name+
       :version     +version+
       :description "Collection of reusable components for my personal use"
       :url         "http://github.com/fmnasution/mur"
       :scm         {:url "http://github.com/fmnasution/mur"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev-repl
  []
  (comp
   (repl :server true)
   (watch)
   (refresh)))
