package com.example.chatgptasistent.model
import com.example.chatgptasistent.*
import com.example.chatgptasistent.CompletionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CompletionInterceptor {
    fun postCompletion(prompt: String, callback: (CompletionResponse) -> Unit) {
        val service = RetrofitInstance.getRetroInstance().create(CompletionService::class.java)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                var message = Message(
                    content = prompt,
                    role = "user"
                )
                val data = CompletionData(
                    List(1) { message },
                    "gpt-3.5-turbo"
                )
                val response = service.getCompletion(data,"Bearer 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")
                callback(response)
            } catch (e: Exception) {
                (e as? HttpException)?.let {

                }
            }
        }
    }
}