# license-finder

[![Build Status](https://img.shields.io/github/actions/workflow/status/scarletcomply/license-finder/ci.yml?branch=main)](https://github.com/scarletcomply/license-finder/actions)
[![cljdoc](https://cljdoc.org/badge/com.scarletcomply/license-finder)][cljdoc]
[![Clojars](https://img.shields.io/clojars/v/com.scarletcomply/license-finder.svg)][clojars]

Finds licenses of your Clojure(Script) dependencies.

Works with:

- `deps.edn`
- `shadow-cljs.edn`
- `package.json` (with npm's `package-lock.json`)

## Usage

Use `scarlet.license-finder.core/find-licenses` to get license information of
dependencies in a project file:

```clojure
(require '[scarlet.license-finder.core :refer [find-licenses]])

(find-licenses "deps.edn")
;; => ({:name "babashka/fs"
;;      :type :mvn
;;      :version "0.3.17"
;;      :license {:id "EPL-1.0"
;;                :name "Eclipse Public License 1.0"
;;                :url "http://opensource.org/licenses/eclipse-1.0.php"}
;;      :path "/home/user/.m2/repository/babashka/fs/0.3.17/fs-0.3.17.jar"}
;;     {:name "io.github.clojure/tools.build"
;;      :type :mvn
;;      :version "0.9.4"
;;      :license {:id "EPL-1.0"
;;                :name "Eclipse Public License 1.0"
;;                :url "https://opensource.org/licenses/eclipse-1.0.php"}
;;      :path "/home/user/.m2/repository/io/github/clojure/tools.build/0.9.4/tools.build-0.9.4.jar"}
;;     ,,,)
```

To consider all dependencies used by your project, add
the `:transitive? true` option:

```clojure
(find-licenses "deps.edn" :transitive? true)
```

You can create a CSV report of your licenses, e.g. in Continuous Integration,
use `scarlet.license-finder.report/write-csv`.

Here is an example on how to integrate license-finder in `tools.build`:

```clojure
(ns build
  (:require [scarlet.license-finder.core :as license-finder]
            [scarlet.license-finder.report :as report]))

(defn licenses [{:keys [project]
                 :or   {project "./deps.edn"}}]
  (->> (license-finder/find-licenses project :transitive? true)
       (report/write-report {:project project})))
```

Now you can generate a license report via the command line:

```
> clojure -T:build licenses
Licenses written to target/licenses/deps.edn.csv
```

## Installation

Releases are available from [Clojars][clojars].

deps.edn:

```clojure
com.scarletcomply/license-finder {:mvn/version "0.1.5"}
```

Leiningen/Boot:

```clojure
[com.scarletcomply/license-finder "0.1.5"]
```

## License

Copyright 2023 [Scarlet]  
Distributed under the [MIT License].

[MIT License]: ./LICENSE
[Scarlet]: https://scarletcomply.com

[cljdoc]: https://cljdoc.org/jump/release/com.scarletcomply/license-finder
[clojars]: https://clojars.org/com.scarletcomply/license-finder
