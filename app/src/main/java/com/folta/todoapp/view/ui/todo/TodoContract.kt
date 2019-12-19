package com.folta.todoapp.view.ui.todo

import com.folta.todoapp.view.BasePresenter
import com.folta.todoapp.view.BaseView

interface TodoContract {
    interface View : BaseView<Presenter>{

    }
    interface Presenter :BasePresenter{

    }
}