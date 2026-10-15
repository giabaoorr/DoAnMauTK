# Thư Mục Sơ Đồ Thiết Kế UML (PlantUML & Mermaid)

Thư mục này chứa toàn bộ sơ đồ lớp và kiến trúc hệ thống phục vụ báo cáo đồ án môn "Mẫu thiết kế".

## Danh Sách File Sơ Đồ PlantUML (.puml)
1. `system-overview.puml`: Sơ đồ kiến trúc toàn hệ thống nối 6 mẫu thiết kế.
2. `factory-method.puml`: Sơ đồ mẫu Factory Method (`NewsParserFactory`).
3. `adapter.puml`: Sơ đồ mẫu Adapter (`RssEntryAdapter`, `JsonArticleAdapter`).
4. `strategy.puml`: Sơ đồ mẫu Strategy (`SortStrategy`, `FilterStrategy`, `ArticleProcessorContext`).
5. `observer.puml`: Sơ đồ mẫu Observer (`NewsFeed`, `UnreadCounter`, `NewsLogger`).
6. `decorator.puml`: Sơ đồ mẫu Decorator (`ArticleDecorator`, `FeaturedArticle`, `BookmarkedArticle`, `SummarizedArticle`).
7. `singleton.puml`: Sơ đồ mẫu Singleton (`CacheManager`).

---

## Sơ Đồ Trực Quan Bằng Mermaid (Hiển thị trực tiếp)

### 1. Sơ Đồ Tổng Thể Hệ Thống (Architecture Overview)

```mermaid
classDiagram
    direction TB
    class Article {
        +String id
        +String title
        +String description
        +String source
        +String category
        +boolean bookmarked
        +boolean featured
        +getFormattedDate()
        +getBadge()
    }

    class NewsService {
        +refreshArticles()
        +getProcessedArticles()
        +applyDecorators()
    }

    class NewsParserFactory {
        <<abstract>>
        +createParser()*
        +getFactory()
    }
    class RssParserFactory {
        +createParser()
    }
    class JsonParserFactory {
        +createParser()
    }
    NewsParserFactory <|-- RssParserFactory
    NewsParserFactory <|-- JsonParserFactory

    class ArticleAdapter {
        <<interface>>
        +adapt(rawData)*
    }
    class RssEntryAdapter {
        +adapt(SyndEntry)
    }
    class JsonArticleAdapter {
        +adapt(JsonNode)
    }
    ArticleAdapter <|.. RssEntryAdapter
    ArticleAdapter <|.. JsonArticleAdapter

    class ArticleProcessorContext {
        +setSortStrategy()
        +setFilterStrategy()
        +process()
    }
    class SortStrategy {
        <<interface>>
        +sort()*
    }
    class FilterStrategy {
        <<interface>>
        +filter()*
    }
    ArticleProcessorContext o-- SortStrategy
    ArticleProcessorContext o-- FilterStrategy

    class NewsSubject {
        <<interface>>
        +attach()
        +detach()
        +notifyObservers()
    }
    class NewsFeed {
        +notifyObservers()
    }
    class NewsObserver {
        <<interface>>
        +onNewsUpdated()*
    }
    class UnreadCounter
    class NewsLogger
    NewsSubject <|.. NewsFeed
    NewsObserver <|.. UnreadCounter
    NewsObserver <|.. NewsLogger
    NewsFeed o-- NewsObserver

    class ArticleDecorator {
        <<abstract>>
        #Article decoratedArticle
    }
    class FeaturedArticle
    class BookmarkedArticle
    class SummarizedArticle
    Article <|-- ArticleDecorator
    ArticleDecorator <|-- FeaturedArticle
    ArticleDecorator <|-- BookmarkedArticle
    ArticleDecorator <|-- SummarizedArticle
    ArticleDecorator o-- Article

    class CacheManager {
        -CacheManager instance$
        +getInstance()$
        +put()
        +get()
    }

    NewsService --> NewsParserFactory : [Factory Method]
    NewsService --> ArticleProcessorContext : [Strategy]
    NewsService --> NewsFeed : [Observer]
    NewsService --> ArticleDecorator : [Decorator]
    NewsService --> CacheManager : [Singleton]
```

### 2. Mẫu Factory Method
```mermaid
classDiagram
    class NewsParser {
        <<interface>>
        +parse(InputStream, source, category)
        +parseUrl(url, source, category)
    }
    class RssNewsParser {
        +parse()
        +parseUrl()
    }
    class JsonNewsParser {
        +parse()
        +parseUrl()
    }
    NewsParser <|.. RssNewsParser
    NewsParser <|.. JsonNewsParser

    class NewsParserFactory {
        <<abstract>>
        +createParser()* NewsParser
        +getFactory(SourceType)$ NewsParserFactory
    }
    class RssParserFactory {
        +createParser() NewsParser
    }
    class JsonParserFactory {
        +createParser() NewsParser
    }
    NewsParserFactory <|-- RssParserFactory
    NewsParserFactory <|-- JsonParserFactory
    RssParserFactory ..> RssNewsParser : creates
    JsonParserFactory ..> JsonNewsParser : creates
```

### 3. Mẫu Adapter
```mermaid
classDiagram
    class Article {
        +String id
        +String title
        +String description
        +String url
        +String imageUrl
        +LocalDateTime publishedDate
    }
    class ArticleAdapter~T~ {
        <<interface>>
        +adapt(T rawData)* Article
    }
    class SyndEntry {
        <<Adaptee Rome RSS>>
        +getTitle()
        +getLink()
        +getDescription()
        +getPublishedDate()
    }
    class JsonNode {
        <<Adaptee Jackson>>
        +get()
        +asText()
    }
    class RssEntryAdapter {
        +adapt(SyndEntry) Article
    }
    class JsonArticleAdapter {
        +adapt(JsonNode) Article
    }
    ArticleAdapter <|.. RssEntryAdapter
    ArticleAdapter <|.. JsonArticleAdapter
    RssEntryAdapter ..> SyndEntry : adapts
    JsonArticleAdapter ..> JsonNode : adapts
    RssEntryAdapter ..> Article : returns
    JsonArticleAdapter ..> Article : returns
```

### 4. Mẫu Strategy
```mermaid
classDiagram
    class SortStrategy {
        <<interface>>
        +sort(List~Article~)* List~Article~
    }
    class NewestSortStrategy {
        +sort()
    }
    class OldestSortStrategy {
        +sort()
    }
    class TitleSortStrategy {
        +sort()
    }
    SortStrategy <|.. NewestSortStrategy
    SortStrategy <|.. OldestSortStrategy
    SortStrategy <|.. TitleSortStrategy

    class FilterStrategy {
        <<interface>>
        +filter(List~Article~)* List~Article~
        +matches(Article)* boolean
    }
    class CategoryFilterStrategy {
        +filter()
    }
    class SourceFilterStrategy {
        +filter()
    }
    class CompositeFilterStrategy {
        +filter()
    }
    class AllPassFilterStrategy {
        +filter()
    }
    FilterStrategy <|.. CategoryFilterStrategy
    FilterStrategy <|.. SourceFilterStrategy
    FilterStrategy <|.. CompositeFilterStrategy
    FilterStrategy <|.. AllPassFilterStrategy

    class ArticleProcessorContext {
        -SortStrategy sortStrategy
        -FilterStrategy filterStrategy
        +setSortStrategy(SortStrategy)
        +setFilterStrategy(FilterStrategy)
        +process(List~Article~) List~Article~
    }
    ArticleProcessorContext o-- SortStrategy
    ArticleProcessorContext o-- FilterStrategy
```

### 5. Mẫu Observer
```mermaid
classDiagram
    class NewsSubject {
        <<interface>>
        +attach(NewsObserver)*
        +detach(NewsObserver)*
        +notifyObservers(List~Article~, int)*
    }
    class NewsFeed {
        -List~NewsObserver~ observers
        +attach(NewsObserver)
        +detach(NewsObserver)
        +notifyObservers(List~Article~, int)
    }
    NewsSubject <|.. NewsFeed

    class NewsObserver {
        <<interface>>
        +onNewsUpdated(NewsUpdateEvent)*
    }
    class UnreadCounter {
        -AtomicInteger lastNewCount
        +onNewsUpdated(NewsUpdateEvent)
        +getLastNewCount() int
    }
    class NewsLogger {
        -List~String~ logHistory
        +onNewsUpdated(NewsUpdateEvent)
        +getLastLog() String
    }
    NewsObserver <|.. UnreadCounter
    NewsObserver <|.. NewsLogger
    NewsFeed o-- NewsObserver

    class NewsUpdateEvent {
        +List~Article~ newArticles
        +int totalCount
        +LocalDateTime timestamp
    }
    NewsFeed ..> NewsUpdateEvent : creates
    NewsObserver ..> NewsUpdateEvent : consumes
```

### 6. Mẫu Decorator
```mermaid
classDiagram
    class Article {
        +isBookmarked() boolean
        +isFeatured() boolean
        +getBadge() String
        +getTitle() String
        +getDescription() String
    }
    class ArticleDecorator {
        <<abstract>>
        #Article decoratedArticle
        +ArticleDecorator(Article)
        +isBookmarked() boolean
        +isFeatured() boolean
        +getBadge() String
    }
    class FeaturedArticle {
        +isFeatured() true
        +getBadge() "NỔI BẬT"
        +getTitle() "★ " + title
    }
    class BookmarkedArticle {
        +isBookmarked() true
        +getBadge() "ĐÃ LƯU"
    }
    class SummarizedArticle {
        +getDescription()
        +getEstimatedReadingTime()
        +getSummary()
    }

    Article <|-- ArticleDecorator
    ArticleDecorator <|-- FeaturedArticle
    ArticleDecorator <|-- BookmarkedArticle
    ArticleDecorator <|-- SummarizedArticle
    ArticleDecorator o-- Article
```

### 7. Mẫu Singleton
```mermaid
classDiagram
    class CacheManager {
        -CacheManager instance$
        -Map~String, CacheEntry~ cache
        -AtomicLong hitCount
        -AtomicLong missCount
        -CacheManager()
        +getInstance()$ CacheManager
        +put(String, List~Article~)
        +get(String) List~Article~
        +invalidate(String)
        +clear()
        +getHitRate() double
    }
    class CacheEntry {
        -List~Article~ articles
        -Instant createdAt
        -Duration ttl
        +isExpired() boolean
    }
    CacheManager *-- CacheEntry
```
