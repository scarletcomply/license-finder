(ns scarlet.license-finder.tool
  (:require [scarlet.license-finder.core :as core]
            [scarlet.license-finder.report :as report]))

(defn report
  [{:keys [project]
    :or   {project "./deps.edn"}
    :as   opts}]
  (let [opts (assoc opts :project project)]
    (->> (core/find-licenses project opts)
         (report/write-report opts))))
