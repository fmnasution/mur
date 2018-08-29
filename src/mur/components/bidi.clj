(ns mur.components.bidi
  (:require
   [clojure.spec.alpha :as s]
   [com.stuartsierra.component :as c]
   [bidi.bidi :as b]
   [bidi.ring :refer [make-handler]]
   [mur.components.ring :as cptrng]
   [mur.components.preparable :as cptprp]))

;; =================================================================
;; router spec
;; =================================================================

(s/def ::not-found-handler
  fn?)

(s/def ::context-keys
  (s/coll-of keyword?))

;; =================================================================
;; endpoint spec
;; =================================================================

(s/def ::routes
  (s/tuple (s/or :path               string?
                 :parameterized-path (s/coll-of (s/or :part  string?
                                                      :param keyword?)))
           (s/or :endpoint fn?
                 :routes   (s/coll-of ::routes))))

;; =================================================================
;; route collector
;; =================================================================

(defn- collect-routes
  [component]
  (if-let [map-route (not-empty
                      (into []
                            (comp
                             (map val)
                             (filter #(satisfies? b/RouteProvider %))
                             (map b/routes))
                            component))]
    map-route
    (throw (ex-info "No RouteProvider to be found" {}))))

(defrecord RouteCollector [prefix routes]
  c/Lifecycle
  (start [this]
    (if (some? routes)
      this
      (assoc this :routes [prefix (collect-routes this)])))
  (stop [this]
    (if (nil? routes)
      this
      (assoc this :routes nil)))

  b/RouteProvider
  (routes [this]
    routes))

(defn make-route-collector
  [prefix]
  (map->RouteCollector {:prefix prefix}))

;; =================================================================
;; ring router
;; =================================================================

(defrecord RingRouter [not-found-handler route-collector]
  cptrng/IWebRequestHandler
  (request-handler [this]
    (let [routes (b/routes route-collector)]
      (some-fn (make-handler routes) not-found-handler)))

  b/RouteProvider
  (routes [this]
    (b/routes route-collector)))

(defn make-ring-router
  [not-found-handler]
  (-> {:not-found-handler (s/assert ::not-found-handler not-found-handler)}
      (map->RingRouter)
      (c/using [:route-collector])))

;; =================================================================
;; context ring router
;; =================================================================

(defn- context-adapter
  [component context-keys]
  (fn [handler]
    (fn [request]
      (let [context (into (select-keys request context-keys)
                          (map (fn [[k v]]
                                 [k (if (cptprp/preparable? v)
                                      (cptprp/prepare v)
                                      v)]))
                          component)]
        ((handler context) request)))))

(defrecord ContextRingRouter [not-found-handler
                              context-keys
                              route-collector]
  cptrng/IWebRequestHandler
  (request-handler [this]
    (some-fn (make-handler
              (b/routes route-collector)
              (context-adapter this context-keys))
             not-found-handler))

  b/RouteProvider
  (routes [this]
    (b/routes route-collector)))

(defn make-context-ring-router
  ([not-found-handler context-keys]
   (-> {:not-found-handler (s/assert ::not-found-handler not-found-handler)
        :context-keys      (s/assert ::context-keys context-keys)}
       (map->ContextRingRouter)
       (c/using [:route-collector])))
  ([not-found-handler]
   (make-context-ring-router not-found-handler [])))

;; =================================================================
;; endpoint
;; =================================================================

(defrecord RingEndpoint [routes]
  b/RouteProvider
  (routes [this] routes))

(defn make-ring-endpoint
  [routes]
  (map->RingEndpoint {:routes (s/assert ::routes routes)}))
