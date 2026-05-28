Кучербаева Анастасия, группа Б9124-09.03.03пикд(4)

Всего: 12 тестов

Юнит-тесты:
1. начальное состояние экрана (initial state is loading)
2. успешная загрузка данных (loadFirstPage success sets characters)
3. ошибка загрузки (loadFirstPage error with no cache shows error message)
4. retry() после ошибки (retry after error loads data successfully)
5. пустой результат поиска (search with empty result sets isEmptySearchResult true)
6. отсутствие дублей (saveSearchResult deletes previous entries before insert to avoid duplicates)
7. корректное преобразование моделей (getCharactersPage converts API response correctly)

интеграционные:
1. DAO + Room (insertAndReadCharacters)
2. Repository + Room (repositoryFetchesFromApiAndSavesToCache)
3. UI: ошибка → Retry → успех (error_thenRetry_showsData)
нетревиальные:
1. retry() инициирует новый запрос(retry calls repository again)
2. отсутствие дублей в кеше(repeated search for same query does not duplicate characters)
