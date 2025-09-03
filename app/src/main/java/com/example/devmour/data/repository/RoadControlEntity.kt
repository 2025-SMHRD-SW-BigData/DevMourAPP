package com.example.devmour.data.db.repository  // 기존 패키지 유지

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_road_control")
data class RoadControlEntity(
    @PrimaryKey(autoGenerate = true) val control_idx: Int = 0,
    val control_addr: String,
    val control_desc: String,
    val control_st_tm: Long,
    val control_type: String? = null,
    val completed: String? = null
)