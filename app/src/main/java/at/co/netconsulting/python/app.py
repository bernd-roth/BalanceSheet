import sys
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
import json
from sqlalchemy import select
from decimal import *
from datetime import date, timedelta
import datetime
import calendar
from sqlalchemy import create_engine
from sqlalchemy.sql import functions
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import text
import os
from dotenv import load_dotenv

load_dotenv()  # Load environment variables from .env file

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv('DATABASE_URL')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Configure database connection for Docker environment
DB_URI = 'postgresql://postgres:3Jkris67zhnnhz76zhn@db:5432/incomeexpense'
engine = create_engine(DB_URI)
Base = declarative_base()

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = DB_URI
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
migrate = Migrate(app, db)

class TransactionLog(db.Model):
	__tablename__ = 'transaction_log'

	transaction_id = db.Column(db.String(50), primary_key=True)
	timestamp = db.Column(db.DateTime, default=datetime.datetime.utcnow)
	processed = db.Column(db.Boolean, default=False)

	def __init__(self, transaction_id):
		self.transaction_id = transaction_id

class IncomeExpenseModel(db.Model):
	__tablename__ = 'incomeexpense'

	id = db.Column(db.Integer, primary_key=True)
	orderdate = db.Column(db.Date)  # Changed from db.String()
	who = db.Column(db.String())
	position = db.Column(db.String)
	income = db.Column(db.Numeric(16,2), nullable=True)
	expense = db.Column(db.Numeric(16,2), nullable=True)
	location = db.Column(db.String, nullable=True)
	comment = db.Column(db.String, nullable=True)

	def __init__(self, orderdate, who, position, income, expense, location, comment):
		self.orderdate = orderdate
		self.who = who
		self.position = position
		self.income = income
		self.expense = expense
		self.location = location
		self.comment = comment

	def __repr__(self):
		return f"IncomeExpenseModel('{self.id}', '{self.orderdate}', '{self.who}', '{self.position}', '{self.income}', '{self.expense}', '{self.location}', '{self.comment}')"

@app.route('/incomeexpense/all', methods=['GET'])
def handle_incomexpense_all():
	if request.method == 'GET':
		response = []
		first_day = datetime.date.today().replace(day=1)
		last_day = datetime.date.today().replace(day=calendar.monthrange(datetime.date.today().year, datetime.date.today().month)[1])
		for incomeexpense in IncomeExpenseModel.query.filter(IncomeExpenseModel.orderdate.between(first_day, last_day)).order_by(IncomeExpenseModel.id.desc()).all():
			response.append({
				"id": incomeexpense.id,
				"orderdate": incomeexpense.orderdate,
				"who": incomeexpense.who,
				"position": incomeexpense.position,
				"income": incomeexpense.income,
				"expense": incomeexpense.expense,
				"location": incomeexpense.location,
				"comment": incomeexpense.comment
			})
		return {"message": "success", "incomeexpense": response}

@app.route('/incomeexpense/put/<id>', methods=['PUT'])
def handle_incomexpense_put(id):
	print("id:", id)
	income_expense = IncomeExpenseModel.query.filter_by(id=id).first()
	if income_expense:
		income_expense.id = request.form['id']
		income_expense.orderdate = request.form['orderdate']
		income_expense.income = request.form['income']
		income_expense.expense = request.form['expense']
		income_expense.who = request.form['who']
		income_expense.position = request.form['position']
		income_expense.location = request.form['location']
		income_expense.comment = request.form['comment']

		db.session.commit()
		return jsonify({"message": "Successfully inserted!"})

def execute_query(query):
	"""Helper function to execute raw SQL queries"""
	with db.engine.connect() as connection:
		result = connection.execute(text(query))
		return result.fetchall()

@app.route('/incomeexpense/sum_expense', methods=['GET'])
def handle_expense_sum():
	query = """
    SELECT SUM(expense)
    FROM incomeexpense
    WHERE position <> 'Income'
    AND orderdate BETWEEN date_trunc('month', current_date)
    AND (date_trunc('month', now()) + interval '1 month - 1 day')::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_income', methods=['GET'])
def handle_incomexpense_sum():
	query = """
    SELECT SUM(income)
    FROM incomeexpense
    WHERE position='Income'
    AND orderdate BETWEEN date_trunc('month', now())
    AND (date_trunc('month', now()) + interval '1 month - 1 day')::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_savings', methods=['GET'])
def handle_incomexpense_sum_savings():
	query = """
    SELECT SUM(income)-SUM(expense)
    FROM incomeexpense
    WHERE orderdate BETWEEN date_trunc('month', current_date)
    AND (date_trunc('month', now()) + interval '1 month - 1 day')::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_food', methods=['GET'])
def handle_incomexpense_sum_food():
	query = """
    SELECT ABS(SUM(income)-SUM(expense))
    FROM incomeexpense
    WHERE position LIKE 'Food%'
    AND orderdate BETWEEN date_trunc('month', current_date)
    AND (date_trunc('month', now()) + interval '1 month - 1 day')::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

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
	if request.method == 'POST' and all(field in request.form for field in ['orderdate', 'who', 'position', 'income', 'expense', 'location', 'comment', 'transaction_id']):
		transaction_id = request.form.get('transaction_id')

		# Check if transaction was already processed
		existing_transaction = TransactionLog.query.filter_by(
			transaction_id=transaction_id,
			processed=True
		).first()

		if existing_transaction:
			return jsonify({
				"message": "Transaction already processed",
				"duplicate": True
			}), 409  # HTTP 409 Conflict

		try:
			# Start database transaction
			db.session.begin_nested()

			# Log the transaction
			transaction_log = TransactionLog(transaction_id=transaction_id)
			db.session.add(transaction_log)

			entry = IncomeExpenseModel(
				orderdate=request.form.get('orderdate'),
				who=request.form.get('who'),
				position=request.form.get('position'),
				income=request.form.get('income'),
				expense=request.form.get('expense'),
				location=request.form.get('location'),
				comment=request.form.get('comment')
			)

			db.session.add(entry)

			# Mark transaction as processed
			transaction_log.processed = True

			# Commit everything
			db.session.commit()
			return jsonify({"message": "Successfully inserted!"})

		except Exception as e:
			db.session.rollback()
			return jsonify({
				"message": "Error processing transaction",
				"error": str(e)
			}), 500

@app.route('/incomeexpense/sum_average_spending_day_of_month', methods=['GET'])
def handle_incomexpense_sum_average_spending_day_of_month():
	query = """
    SELECT ROUND(CAST(FLOAT8 (SUM(expense)/EXTRACT(days FROM date_trunc('day', current_date))) AS NUMERIC),2) AS averageDayPerMonth
    FROM incomeexpense
    WHERE position='Food'
    AND orderdate BETWEEN date_trunc('year', now())
    AND date_trunc('day', current_date)
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_reserved_per_day_until_end_of_month', methods=['GET'])
def handle_incomexpense_sum_reserved_per_day_until_end_of_month():
	query = """
    SELECT ROUND(CAST(FLOAT8 (350-SUM(expense)+SUM(income))/(
        date_part('days', (date_trunc('month', CURRENT_DATE) + interval '1 month - 1 day')::date)
        - EXTRACT(DAY FROM CURRENT_DATE - 1)) AS NUMERIC), 2)
    FROM incomeexpense
    WHERE position='Food'
    AND orderdate::date BETWEEN date_trunc('year', CURRENT_DATE)::date
    AND CURRENT_DATE::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_spending_food_since_beginning_of_year', methods=['GET'])
def handle_incomexpense_sum_spending_food_since_beginning_of_year():
	query = """
    SELECT SUM(expense)-SUM(income)
    FROM incomeexpense
    WHERE position = 'Food'
    AND orderdate BETWEEN date_trunc('year', now())
    AND date_trunc('year', now() + interval '1 year') - interval '1 day'
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_income_year', methods=['GET'])
def handle_sum_income_year():
	query = """
    SELECT SUM(income)
    FROM incomeexpense
    WHERE position = 'Income'
    AND orderdate BETWEEN date_trunc('year', now())
    AND date_trunc('year', now() + interval '1 year') - interval '1 day'
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_spending_food_by_julia_current_month', methods=['GET'])
def handle_incomexpense_sum_spending_food_by_julia_current_month():
	julia_food = request.args.get('julia_food')
	query = f"""
    SELECT {julia_food}SUM(expense)-SUM(income)
    FROM incomeexpense
    WHERE position = 'Food'
    AND who = 'Julia'
    AND orderdate BETWEEN date_trunc('year', now())
    AND date_trunc('year', now() + interval '1 year') - interval '1 day'
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_spending_food_by_bernd_current_month', methods=['GET'])
def handle_incomexpense_sum_spending_food_by_bernd_current_month():
	bernd = request.args.get('bernd_food')
	query = f"""
    SELECT {bernd}SUM(expense)-SUM(income)
    FROM incomeexpense
    WHERE position = 'Food'
    AND who = 'Bernd'
    AND orderdate BETWEEN date_trunc('year', now())
    AND date_trunc('year', now() + interval '1 year') - interval '1 day'
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

@app.route('/incomeexpense/sum_spending_food_per_person_per_month', methods=['GET'])
def handle_incomexpense_sum_spending_food_per_person_per_month():
	person = request.args.get('person')
	reserve = request.args.get('reserve')

	# Add input validation
	if not person or not reserve:
		return jsonify({
			"message": "error",
			"error": "Missing required parameters: person and reserve"
		}), 400

	query = f"""
    SELECT {reserve}SUM(expense)-SUM(income) AS total_food_sum_per_person
    FROM incomeexpense
    WHERE position = 'Food'
    AND who = {person}
    AND orderdate BETWEEN date_trunc('month', now())
    AND (date_trunc('month', now() + interval '1 month') - interval '1 day')::date
    """
	result = execute_query(query)
	return {"message": "success", "incomeexpense": {"Total income": [result[0][0]]}}

if __name__ == '__main__':
	with app.app_context():
		# Create tables if they don't exist
		db.create_all()
	app.run(host="0.0.0.0", port=8080, debug=True)