(ns mur.components.timbre
  (:require
   [taoensso.timbre :as log]
   #?@(:clj  [[clojure.spec.alpha :as s]]
       :cljs [[cljs.spec.alpha :as s]])))

;; =================================================================
;; logger spec
;; =================================================================

(s/def ::level
  #{:trace :debug :info :warn :error :fatal :report})

(s/def ::enabled?
  boolean?)

(s/def ::min-level
  ::level)

(s/def ::rate-limit
  (s/coll-of (s/tuple pos-int? pos-int?)))

(s/def ::fn
  fn?)

(s/def ::async?
  boolean?)

(s/def ::appenders
  (s/map-of keyword? (s/keys :opt-un [::enabled?
                                      ::min-level
                                      ::rate-limit
                                      ::output-fn
                                      ::ns-whitelist
                                      ::ns-blacklist
                                      ::fn
                                      #?@(:clj [::async?
                                                ::timestamp-opts])])))

(s/def ::ns-whitelist
  (s/coll-of string?))

(s/def ::ns-blacklist
  (s/coll-of string?))

(s/def ::middleware
  (s/coll-of fn?))

(s/def ::pattern
  string?)

(s/def ::locale
  keyword?)

(s/def ::timezone
  keyword?)

#?(:clj (s/def ::timestamp-opts
          (s/keys :req-un [::pattern ::locale ::timezone])))

(s/def ::output-fn
  fn?)

(s/def ::option
  (s/keys :req-un [::level
                   ::appenders]
          :opt-un [::ns-whitelist
                   ::ns-blacklist
                   ::ns-log-level
                   ::middleware
                   ::output-fn
                   #?@(:clj [::timestamp-opts])]))

;; =================================================================
;; protocols
;; =================================================================

(defprotocol ILogger
  (log [this level throwable message]))

;; =================================================================
;; logger
;; =================================================================

(defrecord Logger []
  ILogger
  (log [this level throwable message]
    (log/log* this level throwable message)))

(defn make-logger
  [option]
  (-> (s/assert ::option option)
      (select-keys [:level
                    :appenders
                    :ns-whitelist
                    :ns-blacklist
                    :ns-log-level
                    :middleware
                    :timestamp-opts
                    :output-fn])
      (map->Logger)))

(defn trace
  [logger message]
  (log logger :trace nil message))

(defn debug [logger message]
  (log logger :debug nil message))

(defn info
  [logger message]
  (log logger :info nil message))

(defn warn [logger message]
  (log logger :warn nil message))

(defn error
  ([logger message]
   (log logger :error nil message))
  ([logger throwable message]
   (log logger :error throwable message)))

(defn fatal
  ([logger message]
   (log logger :fatal nil message))
  ([logger throwable message]
   (log logger :fatal throwable message)))

(defn report [logger message]
  (log logger :report nil message))
