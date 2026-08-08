sed -i 's/private val personDao: PersonDao/private val personDao: PersonDao, private val besDao: BesDao/g' app/src/main/java/com/example/data/TransactionRepository.kt
sed -i '/val allSavings: Flow/a \    val besPortfolio: Flow<BesPortfolio?> = besDao.getBesPortfolio()' app/src/main/java/com/example/data/TransactionRepository.kt
sed -i '/suspend fun deleteSavingById/a \    suspend fun insertOrUpdateBes(bes: BesPortfolio) = besDao.insertOrUpdate(bes)' app/src/main/java/com/example/data/TransactionRepository.kt
