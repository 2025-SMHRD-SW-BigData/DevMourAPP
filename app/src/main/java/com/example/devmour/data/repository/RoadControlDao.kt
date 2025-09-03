package com.example.devmour.data.db.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadControlDao {

    // 새로운 알림을 DB에 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(control: RoadControlEntity)

    // DB에 저장된 모든 알림을 최신순으로 가져오기
    @Query("SELECT * FROM t_road_control ORDER BY control_st_tm DESC")
    fun getAllControls(): Flow<List<RoadControlEntity>>
    @Query("DELETE FROM t_road_control")
    suspend fun deleteAll()
}