package com.folta.todoapp.data.local

class ToDoRepositoryLocal : ToDoRepository {
    private val dao: ToDoDAO = MyDataBase.db.todoDAO()

    override suspend fun save(todo: ToDo): Int {
        dao.upsert(todo)
        return dao.getNewestId()
    }

    override suspend fun findByDate(dateyyMMDD: String): List<ToDo> {
        return dao.findByDate(dateyyMMDD)
    }

    override suspend fun delete(id: Int) {
        dao.delete(id)
    }

    override suspend fun getExistsDate(): List<String> {
        return dao.getExistsDate()
    }

    override suspend fun find(Id: Int): ToDo? {
        return dao.findById(Id)
    }

    override suspend fun saveSort(todos: Iterable<ToDo>) {
        todos.forEachIndexed { index, toDo ->
            toDo.orderId = 1 + index
        }
        dao.updateViewSort(todos)
    }

}