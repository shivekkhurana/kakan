(ns ubiq.components.graphql-server
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [integrant.core :as ig]
            [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
            [com.walmartlabs.lacinia.pedestal :refer [service-map]]
            [com.walmartlabs.lacinia.schema :as schema]
            [io.pedestal.http :as http]))

(defmethod ig/init-key :graphql-server [_ {:keys [enable-graphiql? port resolver db-fns schema-resource]}]
  (-> schema-resource
      io/resource
      slurp
      edn/read-string
      (attach-resolvers resolver)
      schema/compile
      (service-map {:graphiql enable-graphiql?
                    :app-context {:db-fns db-fns} ;; introduce db-fns in lacinia-app-ctx, used by domain interceptor
                    :port port})
      http/create-server
      http/start))

(defmethod ig/halt-key! :graphql-server [_ server]
  (http/stop server))

