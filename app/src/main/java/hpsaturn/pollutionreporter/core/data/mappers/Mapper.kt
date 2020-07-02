package hpsaturn.pollutionreporter.core.data.mappers

interface Mapper<I, O> {
    operator fun invoke(input: I): O
}