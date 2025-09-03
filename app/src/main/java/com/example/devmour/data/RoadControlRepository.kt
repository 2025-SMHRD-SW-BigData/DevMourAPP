package com.example.devmour.data

import com.example.devmour.data.db.repository.RoadControlDao
import com.example.devmour.data.db.repository.RoadControlEntity
import kotlinx.coroutines.flow.Flow

class RoadControlRepository(private val dao: RoadControlDao) {

    // 새로운 알림을 DB에 삽입
    suspend fun insert(control: RoadControlEntity) {
        dao.insert(control)
    }

    // DB에 저장된 모든 알림을 최신순으로 가져오기
    fun getAllControls(): Flow<List<RoadControlEntity>> {
        return dao.getAllControls()
    }
}