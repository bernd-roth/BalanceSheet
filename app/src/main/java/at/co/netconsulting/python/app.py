from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
import json
from sqlalchemy import select
from decimal import *
#date
from datetime import date, timedelta
import datetime
import calendar
#date
#
from sqlalchemy import create_engine
from sqlalchemy.sql import functions
engine = create_engine('postgresql+psycopg2://postgres:password@ip-address:5432/incomeexpense')
from sqlalchemy.ext.declarative import declarative_base
Base = declarative_base()
#

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgres:password@ip-address:5432/incomeexpense"
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
migrate = Migrate(app, db)

class IncomeExpenseModel(db.Model):
	__tablename__ = 'incomeexpense'

	id = db.Column(db.Integer, primary_key=True)
	orderdate = db.Column(db.String())
	who = db.Column(db.String())
	position = db.Column(db.String)
	income = db.Column(db.Numeric(16,2), nullable=True)
	expense = db.Column(db.Numeric(16,2), nullable=True)
	location = db.Column(db.String, nullable=True)

	def __init__(self, orderdate, who, position, income, expense, location):
		self.orderdate = orderdate
		self.who = who
		self.position = position
		self.income = income
		self.expense = expense
		self.location = location

	def __repr__(self):
		return f"IncomeExpenseModel('{self.id}', '{self.orderdate}', '{self.who}', '{self.position}', '{self.income}', '{self.expense}', '{self.location}')"

@app.route('/incomeexpense/all', methods=['GET'])
def handle_incomexpense_all():
	incomeexpense = IncomeExpenseModel.query.order_by(IncomeExpenseModel.orderdate.desc()).all();

	if request.method == 'GET':
		response = []
#		for incomeexpense in IncomeExpenseModel.query.all():
		first_day = datetime.date.today().replace(day=1)
		last_day = datetime.date.today().replace(day=calendar.monthrange(datetime.date.today().year, datetime.date.today().month)[1])
		for incomeexpense in IncomeExpenseModel.query.filter(IncomeExpenseModel.orderdate.between(first_day, last_day)).order_by(IncomeExpenseModel.orderdate.desc()).all():
#		for incomeexpense in IncomeExpenseModel.query.order_by(IncomeExpenseModel.orderdate.desc()).all():
			response.append({
			"orderdate": incomeexpense.orderdate,
			"who": incomeexpense.who,
			"position": incomeexpense.position,
			"income": incomeexpense.income,
			"expense": incomeexpense.expense,
			"location": incomeexpense.location
		})
		return {"message": "success", "incomeexpense": response}

@app.route('/incomeexpense/sum_expense', methods=['GET'])
def handle_expense_sum():
	import collections
	import psycopg2

	conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
	conn = psycopg2.connect(conn_string)
	cursor = conn.cursor()
	cursor.execute("SELECT sum(expense) FROM incomeexpense WHERE position <> 'Income' AND orderdate BETWEEN date_trunc('month', current_date) AND (date_trunc('month', now()) + interval '1 month - 1 day')::date;")
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
	cursor.execute("SELECT SUM(income) FROM incomeexpense WHERE position='Income' AND orderdate BETWEEN date_trunc('month', current_date) AND (date_trunc('month', now()) + interval '1 month - 1 day')::date;")
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
	cursor.execute("SELECT SUM(income)-SUM(expense) FROM incomeexpense WHERE orderdate BETWEEN date_trunc('month', current_date) AND (date_trunc('month', now()) + interval '1 month - 1 day')::date;")
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
#	cursor.execute("SELECT ROUND(ABS(SUM(income)-SUM(expense))/EXTRACT(DAY FROM TIMESTAMP 'NOW()')::numeric,2) FROM incomeexpense WHERE position LIKE 'Food%'")
	cursor.execute("SELECT ABS(SUM(income)-SUM(expense)) FROM incomeexpense WHERE position LIKE 'Food%' and orderdate BETWEEN date_trunc('month', current_date) AND (date_trunc('month', now()) + interval '1 month - 1 day')::date;")
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
			"position": incomeexpense.position,
			"income": incomeexpense.income,
			"expense": incomeexpense.expense,
			"location": incomeexpense.location
		}
		return {"message": "success", "incomeexpense": response}

@app.route('/incomeexpense/add', methods=['POST'])
def handle_incomexpense_add():
	if request.method == 'POST' and 'orderdate' in request.form and 'who' in request.form and 'position' in request.form and 'income' in request.form and 'expense' in request.form and 'location' in request.form:
		orderdate = request.form.get('orderdate')
		who = request.form.get('who')
		position = request.form.get('position')
		income = request.form.get('income')
		expense = request.form.get('expense')
		location = request.form.get('location')
		entry = IncomeExpenseModel(orderdate, who, position, income, expense, location)
		db.session.add(entry)
		db.session.commit()
		return jsonify({"message":"Successfully inserted!"})

#@app.route('/incomeexpense/all', methods=['GET'])
#def handle_incomexpense_all():
#	import collections
#	import psycopg2

#	conn_string = "host='localhost' dbname='incomeexpense' user='postgres' password='password'"
#	conn = psycopg2.connect(conn_string)
#	cursor = conn.cursor()
#	cursor.execute("SELECT orderdate, who, position, income, expense FROM incomeexpense order by orderdate DESC")
#	rows = cursor.fetchall()
#	rowarray_list = []
#	for row in rows:
#		t = (row[0])
#		rowarray_list.append(t)
#	print(rowarray_list[0])
#
#	my_dict = {"Total income":[]};
#	my_dict["Total income"].append(rowarray_list[0])
#	print(my_dict)
#	return {"message": "success", "incomeexpense": my_dict}

if __name__ == '__main__':
	app.run(host="0.0.0.0", debug=True)
