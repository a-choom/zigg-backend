//package soma.achoom.zigg.global.aspect
//
//import com.amazonaws.util.IOUtils
//import jakarta.servlet.ReadListener
//import jakarta.servlet.ServletInputStream
//import jakarta.servlet.http.HttpServletRequest
//import jakarta.servlet.http.HttpServletRequestWrapper
//import java.io.ByteArrayInputStream
//import java.io.ByteArrayOutputStream
//
//class CachedHttpServletRequest(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {
//    private lateinit var cachedBytes:ByteArrayOutputStream
//
//    override fun getInputStream(): ServletInputStream {
//    }
//
//    private fun cachedInputStream(){
//        cachedBytes = ByteArrayOutputStream()
//        IOUtils.copy(super.getInputStream(),cachedBytes)
//    }
//
//    companion object{
//        private class CachedServletInputStream(
//            contents : Array<Byte>
//        ) : ServletInputStream(){
//            private lateinit var buffer: ByteArrayInputStream
//
//            override fun read(): Int {
//                TODO("Not yet implemented")
//            }
//
//            override fun isFinished(): Boolean {
//                TODO("Not yet implemented")
//            }
//
//            override fun isReady(): Boolean {
//                TODO("Not yet implemented")
//            }
//
//            override fun setReadListener(listener: ReadListener?) {
//                TODO("Not yet implemented")
//            }
//
//        }
//    }
//}