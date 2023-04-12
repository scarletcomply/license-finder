(ns scarlet.license-finder.core
  (:require [clojure.string :as str]
            [scarlet.license-finder.deps :as deps]
            [scarlet.license-finder.detect :as detect]
            [scarlet.license-finder.licenses :as licenses]
            [scarlet.license-finder.resource :as resource]))

(defn resource-resolver
  "Returns a resolver function from an EDN resource.

   The EDN file must be a map `dep-name => license-map`, and every license-map
   should contain the following keys:

   | Key | Description |
   |-----|-------------|
   | `:url` | URL to the code repository or other source that names the license. |
   | `:versions` | Vector of version _prefixes_ this license is valid for. |
   | `:license` | License ID (SPDX) or map with `:id`, `:name`, and `:url`. |"
  [resource-name]
  (let [data (resource/read-edn-resource resource-name)]
    (fn [{:keys [name version]}]
      (when-let [{:keys [versions license]} (get data name)]
        (when (or (nil? versions)
                  (some (partial str/starts-with? version) versions))
          (if (map? license)
            license
            {:id license}))))))

(defn- process-license [dep {:keys [resolve-license]}]
  (if-let [license (or (:license dep)
                       (when resolve-license
                         (resolve-license dep))
                       (detect/detect-license (:path dep)))]
    (assoc dep :license (or (licenses/find-license license) license))
    dep))

(defn find-licenses
  "Finds licenses for `project-file`.

   Supported `opts`:

   | Option | Description |
   |--------|-------------|
   | `:transitive?` | Whether to consider transitive dependencies |"
  [project-file & {:as opts}]
  (->> (deps/find-dependencies project-file opts)
       (map #(process-license % opts))
       (sort-by :name)))
