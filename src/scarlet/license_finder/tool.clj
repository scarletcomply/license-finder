(ns scarlet.license-finder.tool
  (:require
   [scarlet.license-finder.core :as core]
   [scarlet.license-finder.report :as report]))

(defn report
  [{:keys [project format path]
    :or   {project "./deps.edn"
           format :csv
           path "./target/licenses/deps.csv"}
    :as options}]
  (let [opts (merge options {:project project
                             :format format
                             :path path})]
    (->> (core/find-licenses project opts)
         (report/generate-report opts))))
