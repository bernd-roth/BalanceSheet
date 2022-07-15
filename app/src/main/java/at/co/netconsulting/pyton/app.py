from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
import json
from sqlalchemy import select
from decimal import *
#
from sqlalchemy import create_engine
from sqlalchemy.sql import functions
engine = create_engine('postgresql+psycopg2://postgres:password@192.168.0.18:5432/incomeexpense')
from sqlalchemy.ext.declarative import declarative_base
Base = declarative_base()
#

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:password@192.168.0.18:5432/incomeexpense"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
migrate = Migrate(app, db)

class IncomeExpenseModel(db.Model):
        __tablename__ = 'incomeexpense'

        id = db.Column(db.Integer, primary_key=True)
        orderdate = db.Column(db.String())
        who = db.Column(db.String())
        location = db.Column(db.Integer())
        income = db.Column(db.Numeric(16,2), nullable=True)
        expense = db.Column(db.Numeric(16,2), nullable=True)

        def __init__(self, orderdate, who, location, income, expense):
                self.orderdate = orderdate
                self.who = who
                self.location = location
                self.income = income
                self.expense = expense

        def __repr__(self):
                return f"IncomeExpenseModel('{self.id}', '{self.orderdate}', '{self.who}', '{self.location}', '{self.income}', '{self.expense}')"

@app.route('/incomeexpense/all', methods=['GET'])
def handle_incomexpense_all():
        incomeexpense = IncomeExpenseModel.query.all();

        if request.method == 'GET':
                response = []
                for incomeexpense in IncomeExpenseModel.query.all():
                        response.append({
                                "orderdate": incomeexpense.orderdate,
                                "who": incomeexpense.who,
                                "location": incomeexpense.location,
                                "income": incomeexpense.income,
                                "expense": incomeexpense.expense
                        })
                return {"message": "success", "incomeexpense": response}

@app.route('/incomeexpense/sum_expense', methods=['GET'])
def handle_expense_sum():
        import collections
        import psycopg2

        conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
        conn = psycopg2.connect(conn_string)
        cursor = conn.cursor()
        cursor.execute("SELECT sum(expense) FROM incomeexpense")
        rows = cursor.fetchall()
        rowarray_list = []
        for row in rows:
                t = (row[0])
                rowarray_list.append(t)
        print(rowarray_list[0])

        my_dict = {"Total income":[]};
        my_dict["Total income"].append(rowarray_list[0])
        print(my_dict)

        return {"message": "success", "incomeexpense": my_dict}

@app.route('/incomeexpense/sum_income', methods=['GET'])
def handle_incomexpense_sum():
        import collections
        import psycopg2

        conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
        conn = psycopg2.connect(conn_string)
        cursor = conn.cursor()
        cursor.execute("SELECT SUM(income) FROM incomeexpense")
        rows = cursor.fetchall()
        rowarray_list = []
        for row in rows:
                t = (row[0])
                rowarray_list.append(t)
        print(rowarray_list[0])

        my_dict = {"Total income":[]};
        my_dict["Total income"].append(rowarray_list[0])
        print(my_dict)

        return {"message": "success", "incomeexpense": my_dict}

@app.route('/incomeexpense/sum_savings', methods=['GET'])
def handle_incomexpense_sum_savings():
        import collections
        import psycopg2

        conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
        conn = psycopg2.connect(conn_string)
        cursor = conn.cursor()
        cursor.execute("SELECT SUM(income)-SUM(expense) FROM incomeexpense")
        rows = cursor.fetchall()
        rowarray_list = []
        for row in rows:
                t = (row[0])
                rowarray_list.append(t)
        print(rowarray_list[0])

        my_dict = {"Total income":[]};
        my_dict["Total income"].append(rowarray_list[0])
        print(my_dict)

        return {"message": "success", "incomeexpense": my_dict}

@app.route('/incomeexpense/sum_food', methods=['GET'])
def handle_incomexpense_sum_food():
        import collections
        import psycopg2

        conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
        conn = psycopg2.connect(conn_string)
        cursor = conn.cursor()
        cursor.execute("SELECT ROUND(ABS(SUM(income)-SUM(expense))/EXTRACT(DAY FROM TIMESTAMP 'NOW()')::numeric,2) FROM incomeexpense WHERE location LIKE 'Food%'")
        rows = cursor.fetchall()
        rowarray_list = []
        for row in rows:
                t = (row[0])
                rowarray_list.append(t)
        print(rowarray_list[0])

        my_dict = {"Total income":[]};
        my_dict["Total income"].append(rowarray_list[0])
        print(my_dict)

        return {"message": "success", "incomeexpense": my_dict}

@app.route('/incomeexpense/<id>', methods=['GET'])
def handle_incomexpense(id):
        incomeexpense = IncomeExpenseModel.query.get_or_404(id)

        if request.method == 'GET':
                response = {
                        "orderdate": incomeexpense.orderdate,
                        "who": incomeexpense.who,
                        "location": incomeexpense.location,
                        "income": incomeexpense.income,
                        "expense": incomeexpense.expense
                }
                return {"message": "success", "incomeexpense": response}

@app.route('/incomeexpense/add', methods=['POST'])
def handle_incomexpense_add():
        if request.method == 'POST' and 'orderdate' in request.form and 'who' in request.form and 'location' in request.form and 'income' in request.form and 'expense' in request.form:
                orderdate = request.form.get('orderdate')
                who = request.form.get('who')
                location = request.form.get('location')
                income = request.form.get('income')
                expense = request.form.get('expense')
                entry = IncomeExpenseModel(orderdate, who, location, income, expense)
                db.session.add(entry)
                db.session.commit()
                return jsonify({"message":"Successfully inserted!"})

if __name__ == '__main__':
        app.run(host="0.0.0.0", debug=True)