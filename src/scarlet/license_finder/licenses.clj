(ns scarlet.license-finder.licenses
  (:require [scarlet.license-finder.resource :as resource]))

(def spdx-resource-name
  "scarlet/license_finder/spdx.edn")

(def ^:private license-abbrev
  "tools.deps comes with a map `name => id` of common open source
   license names and their abbreviation (SPDX License ID).
   Handles alternative spellings and is much shorter than the SPDX list."
  (delay (resource/read-edn-resource "clojure/tools/deps/license-abbrev.edn")))

(defn- index-spdx-licenses [licenses]
  {:by-id   licenses
   :by-name (into {} (map (juxt :name identity)) (vals licenses))
   :by-url  (into {}
                  (mapcat (fn [{:keys [ref see-also] :as license}]
                            (map #(vector % license) (cons ref see-also))))
                  (vals licenses))})

(def ^:private spdx-licenses
  (delay (-> spdx-resource-name resource/read-edn-resource index-spdx-licenses)))

(defn license-name [id]
  (get-in @spdx-licenses [:by-id id :name]))

(defn license-url [id]
  (when-let [{:keys [ref see-also]} (get-in @spdx-licenses [:by-id id])]
    ;; :see-also links to the "source", while :ref is a link to the
    ;; SPDX website.
    (or (first see-also) ref)))

(defn find-license-id [{:keys [id name url]}]
  (or id
      (get @license-abbrev name)
      (get-in @spdx-licenses [:by-id name :id])
      (get-in @spdx-licenses [:by-url url :id])
      (get-in @spdx-licenses [:by-name name :id])))

(defn find-license [license]
  (when-let [id (find-license-id license)]
    (cond-> (assoc license :id id)
      (not (:name license)) (assoc :name (license-name id))
      (not (:url license))  (assoc :url  (license-url id)))))

(defn common-license-names
  "Returns a map of license names to license IDs."
  []
  @license-abbrev)
