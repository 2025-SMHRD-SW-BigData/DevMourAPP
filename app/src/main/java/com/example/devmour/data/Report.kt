package com.example.devmour.data

data class Report(
    val lat: Double? = null,
    val lon: Double? = null,
    val addr: String? = null,
    val c_report_detail: String,
    val c_report_file1: String? = null,
    val c_report_file2: String? = null,
    val c_report_file3: String? = null,
    val c_reporter_name: String? = null,
    val c_reporter_phone: String? = null
)

data class ReportResponse(
    val success: Boolean,
    val message: String,
    val data: ReportData? = null
)

data class ReportData(
    val reportId: Int,
    val addr: Any?, // String 또는 객체일 수 있음
    val c_report_detail: String,
    val files: Map<String, String?>
)
