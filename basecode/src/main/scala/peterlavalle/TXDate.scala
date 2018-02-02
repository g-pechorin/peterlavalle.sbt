package peterlavalle

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

trait TXDate {

	implicit class TWrappedDate(date: Date) {
		def iso8061Day: String = {
			import java.util.Locale
			val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
			dateFormat.format(date)
		}
	}

}
