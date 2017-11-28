import javax.inject.{Inject, Singleton}

import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter
import v2.filters.InfoFilter

@Singleton
class Filters @Inject()(gzipFilter: GzipFilter,
                        buildInfoFilter: InfoFilter)
  extends DefaultHttpFilters(gzipFilter, buildInfoFilter)
