package nl.moukafih.mynfc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyViewModel {

    private val _readingStatus = MutableStateFlow("Idle") // Initial status
    val readingStatus: StateFlow<String> = _readingStatus

    fun updateReadingStatus(status: String) {
        _readingStatus.value = status
    }

    private val _techList = MutableStateFlow("")
    val techList: StateFlow<String> = _techList

    private val _serialNumber = MutableStateFlow("")
    val serialNumber: StateFlow<String> = _serialNumber

    private val _atqa = MutableStateFlow("")
    val atqa: StateFlow<String> = _atqa

    private val _sak = MutableStateFlow("")
    val sak: StateFlow<String> = _sak

    private val _manufacturer = MutableStateFlow("")
    val manufacturer: StateFlow<String> = _manufacturer

    private val _product = MutableStateFlow("")
    val product: StateFlow<String> = _product

    fun updateManufacturer(manufacturer: String) {
        _manufacturer.value = manufacturer
    }

    fun updateAtqa(atqa: String) {
        _atqa.value = atqa
    }

    fun updateSak(sak: String) {
        _sak.value = sak
    }

    fun updateSerialNumber(serialNumber: String) {
        _serialNumber.value = serialNumber
    }

    fun updateTechList(techList: String) {
        _techList.value = techList
    }
}


