package nl.moukafih.mynfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.MifareClassic
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import nl.moukafih.mynfc.ui.theme.MyNFCTheme

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    companion object {
        private val TAG : String = MainActivity::class.java.simpleName
    }

    private val nfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }

    private val viewModel = MyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"onCreate")

        setContent {
            MyNFCTheme {
                MainScreen(this, viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume")

        if (nfcAdapter == null) {
            Log.d(TAG, "nfcAdapter is null")
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Log.d(TAG, "nfcAdapter is not Enabled")
            return
        }

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        nfcAdapter!!.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            options
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"onPause")

        nfcAdapter?.disableForegroundDispatch(this)
    }


    override fun onTagDiscovered(tag: Tag) {
        // Update basic tag information
        updateTagInfo(tag)

        viewModel.updateReadingStatus("Reading...")

        // Handle specific tag technologies
        tag.techList.forEach { tech ->
            when (tech) {
                IsoDep::class.java.name -> handleIsoDep(IsoDep.get(tag))
                MifareClassic::class.java.name -> handleMifareClassic(MifareClassic.get(tag))
                NfcA::class.java.name -> handleNfcA(NfcA.get(tag))
                MifareUltralight::class.java.name -> handleMifareUltralight(MifareUltralight.get(tag))
                Ndef::class.java.name -> handleNdef(Ndef.get(tag))
                else -> Log.d(TAG, "Unsupported Technology: $tech")
            }
        }
    }

    private fun updateTagInfo(tag: Tag) {

        val techList = tag.techList.map { it.substringAfterLast('.') }
        val serialNumber = tag.id.joinToString(":") { "%02X".format(it) }

        Log.d(TAG, "Technologies: ${techList.joinToString()}")
        Log.d(TAG, "Serial Number: $serialNumber")

        viewModel.updateTechList(techList.joinToString())
        viewModel.updateSerialNumber(serialNumber)
    }

    private fun handleIsoDep(isoDep: IsoDep?) {
        Log.d("NFC", "handleIsoDep")

        isoDep?.apply {
            try {
                connect()
                val versionResponse = transceive(byteArrayOf(0x60.toByte()))
                Log.d(TAG, "IsoDep Version: ${versionResponse.joinToString { it.toString(16) }}")

                val uidResponse = transceive(byteArrayOf(0x30.toByte()))
                Log.d(TAG, "IsoDep UID: ${uidResponse.joinToString { it.toString(16) }}")

                viewModel.updateReadingStatus("Reading Completed")

            } catch (e: Exception) {
                Log.e(TAG, "IsoDep Error: ${e.message}")
                viewModel.updateReadingStatus("Error: ${e.message}")
            } finally {
                close()
            }
        }
    }

    private fun handleMifareClassic(mifareClassic: MifareClassic?) {
        Log.d("NFC", "handleMifareClassic")

        Log.d("NFC", "Tag Type: ${mifareClassic?.type}")
        Log.d("NFC", "Tag Size: ${mifareClassic?.size}")
        Log.d("NFC", "Tag Sector Count: ${mifareClassic?.sectorCount}")
        Log.d("NFC", "Tag Block Count: ${mifareClassic?.blockCount}")

        mifareClassic?.apply {
            try {
                connect()

                for (sectorIndex in 0 until mifareClassic.sectorCount) {
                    var isAuthenticated = false

                    if (mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        isAuthenticated = true;
                    } else if (mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)) {
                        isAuthenticated = true;
                    } else if (mifareClassic.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_NFC_FORUM)) {
                        isAuthenticated = true;
                    } else {
                        Log.d("TAG", "Authorization denied Sector $sectorIndex");
                    }

                    if(isAuthenticated) {
                        val blockIndex = mifareClassic.sectorToBlock(sectorIndex)
                        val block = mifareClassic.readBlock(blockIndex)
                        Log.d(TAG, "MifareClassic Response Sector $sectorIndex: ${block.joinToString { it.toString(16) }}")
                    }
                }

                /*for (sector in 0 until sectorCount) {
                    if (authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT) or
                        authenticateSectorWithKeyA(sector, MifareClassic.KEY_NFC_FORUM) or
                        authenticateSectorWithKeyA(sector, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        val blockIndex = sectorToBlock(sector)
                        val block = readBlock(blockIndex)
                        Log.d(TAG, "Sector $sector Data: ${block.joinToString { it.toString(16) }}")
                    } else {
                        Log.d(TAG, "Authentication failed for sector $sector")
                    }
                }*/

                viewModel.updateReadingStatus("Reading Completed")

            } catch (e: Exception) {
                Log.e(TAG, "MifareClassic Error: ${e.message}")
                viewModel.updateReadingStatus("Error: ${e.message}")
            } finally {
                close()
            }
        }
    }

    private fun handleNfcA(nfcA: NfcA?) {
        Log.d("NFC", "handleNfcA")

        nfcA?.apply {
            try {
                connect()
                val atqa = atqa.reversedArray().joinToString("") { "%02X".format(it) }
                val sak = "%02X".format(sak)

                Log.d(TAG, "ATQA: $atqa, SAK: $sak")
                viewModel.updateAtqa("0x$atqa")
                viewModel.updateSak("0x$sak")


                // Example: Send and receive data (requires knowledge of tag-specific protocol)
                val command = byteArrayOf(0x30, 0x00) // Example command (Read command for Mifare Ultralight)
                val response = transceive(command)
                Log.d("NFC", "Response: ${response.joinToString(", ") { it.toString(16) }}")

                viewModel.updateReadingStatus("Reading Completed")
            } catch (e: Exception) {
                Log.e(TAG, "NfcA Error: ${e.message}")
                viewModel.updateReadingStatus("Error: ${e.message}")
            } finally {
                close()
            }
        }
    }

    private fun handleMifareUltralight(mifareUltralight: MifareUltralight?) {
        Log.d("NFC", "handleMifareUltralight type: ${mifareUltralight?.type}")

        mifareUltralight?.apply {
            try {
                connect()
                val pageData = readPages(4)
                Log.d(TAG, "MifareUltralight Page 4: ${pageData.joinToString { it.toString(16) }}")
                viewModel.updateReadingStatus("Reading Completed")
            } catch (e: Exception) {
                Log.e(TAG, "MifareUltralight Error: ${e.message}")
                viewModel.updateReadingStatus("Error: ${e.message}")
            } finally {
                close()
            }
        }
    }

    private fun handleNdef(ndef: Ndef?) {
        Log.d("NFC", "handleNdef")

        ndef?.apply {
            try {
                connect()
                ndefMessage?.records?.forEach { record ->
                    val payload = String(record.payload, Charsets.UTF_8)
                    Log.d(TAG, "NDEF Record: $payload")
                }

                viewModel.updateReadingStatus("Reading Completed")
            } catch (e: Exception) {
                Log.e(TAG, "NDEF Error: ${e.message}")
                viewModel.updateReadingStatus("Error: ${e.message}")
            } finally {
                close()
            }
        }
    }
}



