import os
import time
import requests  
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup
from urllib.parse import urljoin

# URL of the initial page
base_url = 'https://kra.go.ke/publications'

download_directory = os.path.join(os.path.expanduser("~"), "Desktop", "kra_data")

os.makedirs(download_directory, exist_ok=True)

driver = webdriver.Chrome()  

# Visit the initial page 
driver.get(base_url)

WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CLASS_NAME, 'pagination')))

for page_number in range(1, 51):
    # Using the link page id to with an incriment as my target to open another Tab
    pagination_element = driver.find_element(By.XPATH, f'//a[@id="filterpag-{page_number}"]')

    pagination_element.click()

    time.sleep(5)

    page_source = driver.page_source

    soup = BeautifulSoup(page_source, 'html.parser')

    links = soup.find_all('a')

    for link in links:
        href = link.get('href')
        if href and href.endswith('.pdf'):
            absolute_url = urljoin(base_url, href)

            response = requests.get(absolute_url)
            filename = absolute_url.split('/')[-1]

            with open(os.path.join(download_directory, filename), 'wb') as f:
                f.write(response.content)
                print(f"Downloaded: {filename}")

driver.quit()
