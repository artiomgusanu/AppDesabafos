package ip.santarem.myapplication

data class Post(
    val content: String,      // O texto do desabafo
    val imageUri: String?,    // O URI da imagem (pode ser nulo)
    val userName: String,     // Nome do autor do post
    val timestamp: String,    // Data e hora do post
    val categoria: String     // Categoria do post
)


